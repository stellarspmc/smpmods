package spmc.smpmod.registry

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.EconomyData
import spmc.smpmod.economy.fluctuate.MarketState
import spmc.smpmod.fishing.FishTracker
import spmc.smpmod.npc.NPCData
import spmc.smpmod.npc.NPCManager
import spmc.smpmod.quest.PlayerQuestData.ActiveQuest
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.QuestManager
import spmc.smpmod.utils.MessageUtils.sendError
import spmc.smpmod.utils.MessageUtils.sendSuccess
import spmc.smpmod.utils.UtilityFunctions.streamToSuggestion
import spmc.smpmod.vault.VaultData
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.NameAndId
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.levelgen.Heightmap
import spmc.smpmod.utils.UtilityFunctions.isAdmin
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object CommandRegistry {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, context: CommandBuildContext) {
        dispatcher.register(buildBalanceNode("bal"))
        dispatcher.register(buildBalanceNode("balance"))
        dispatcher.register(Commands.literal("baltop")
            .executes { executeTop(it, 1) }
            .then(Commands.argument<Int>("page", IntegerArgumentType.integer(1))
				.executes { executeTop(it, IntegerArgumentType.getInteger(it, "page")) }))

        dispatcher.register(Commands.literal("send")
            .then(Commands.argument("player", GameProfileArgument.gameProfile())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                    .executes(CommandRegistry::executeSend))))

        dispatcher.register(Commands.literal("deposit")
            .executes(CommandRegistry::executeDepositHand)
            .then(Commands.literal("all")
                .executes(CommandRegistry::executeDepositAll)))

        dispatcher.register(Commands.literal("market")
            .executes(CommandRegistry::executeMarketAll)
            .then(Commands.argument("item", ItemArgument.item(context)).suggests(streamToSuggestion(MarketState.state?.all?.keys ?: setOf()))
                .executes(CommandRegistry::executeMarketItem)))

        dispatcher.register(Commands.literal("withdraw")
            .then(Commands.argument("item", ItemArgument.item(context))
                .suggests(streamToSuggestion(MarketState.state?.all?.keys ?: setOf())) // TODO: add diamonds
                .executes { executeWithdraw(it, 1) }
                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                    .executes { executeWithdraw(it, IntegerArgumentType.getInteger(it, "count")) })))

        dispatcher.register(Commands.literal("mapart")
            .then(Commands.argument("url", StringArgumentType.greedyString())
                .executes(CommandRegistry::executeMapArt)))

        dispatcher.register(Commands.literal("npc")
            .then(Commands.literal("kill")
                .then(Commands.argument("id", StringArgumentType.greedyString())
                    .requires { isAdmin(it.player?: return@requires true) } // supposedly cmd blocks or console
                    .suggests { _, builder -> SharedSuggestionProvider.suggest(NPCManager.allIds, builder) }
                    .executes(CommandRegistry::executeNpcKill)))
            .then(Commands.literal("setup")
                .then(Commands.argument<String>("id", StringArgumentType.greedyString())
                    .requires { isAdmin(it.player?: return@requires false) }
                    .suggests { _, builder -> SharedSuggestionProvider.suggest(NPCManager.allIds, builder) }
                    .executes(CommandRegistry::executeNpcSetup))))

        dispatcher.register(Commands.literal("fishing").executes { FishTracker.openFishIndexMenu(it.getSource().player?: return@executes -1) })
        dispatcher.register(Commands.literal("vault").executes { VaultData.sendVaultMessage(it.getSource().player?: return@executes -1) })
        dispatcher.register(Commands.literal("quests").executes(CommandRegistry::executeQuests))
        dispatcher.register(Commands.literal("surface").executes(CommandRegistry::executeSurface))
        dispatcher.register(Commands.literal("enderchest") .executes(CommandRegistry::executeEnderChest))
    }

    private fun buildBalanceNode(name: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { executeBalance(it, NameAndId((it.getSource().player?: return@executes -1).gameProfile)) }
            .then(Commands.argument("player", GameProfileArgument.gameProfile())
            .executes { executeBalance(it, GameProfileArgument.getGameProfiles(it, "player").iterator().next()) })
    }

    private fun executeBalance(ctx: CommandContext<CommandSourceStack>, target: NameAndId): Int {
        val eco: EconomyData = EconomyData.get() ?: return -1
        ctx.getSource().sendSuccess({ Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(target.name() + " has ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", eco.getBalance(target.id()))).withStyle(ChatFormatting.RED))
                .append(Component.literal(".").withStyle(ChatFormatting.GOLD))
        }, false)
        return 1
    }

    private fun executeTop(ctx: CommandContext<CommandSourceStack>, page: Int): Int {
        val eco: EconomyData = EconomyData.get() ?: return -1
        ctx.getSource().sendSuccess({ Component.literal("Wealth Leaderboard").withStyle(ChatFormatting.GOLD).append("\n").append(eco.getMinecraftTop(page)) }, false)
        return 1
    }

    private fun executeSend(ctx: CommandContext<CommandSourceStack>): Int {
        val target = GameProfileArgument.getGameProfiles(ctx, "player").iterator().next()
        val sender = ctx.getSource().player?: return -1
        val amount = ((DoubleArgumentType.getDouble(ctx, "amount") * 100f).roundToInt() / 100f).toDouble()
        if (sender.getUUID() == target.id()) return sendError(sender, "You cannot send money to yourself.")

        val eco: EconomyData = EconomyData.get() ?: return -1
        if (eco.changeBalance(sender.getUUID(), -amount)) {
            eco.changeBalance(target.id(), amount)

            SMPMod.minecraftServer?.playerList?.getPlayer(target.id())?.sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(sender.name.string).withStyle(ChatFormatting.RED))
            )

            return sendSuccess(sender, String.format("Sent $%.2f to %s.", amount, target.name()))
        }

        return sendError(sender, "Insufficient funds.")
    }

    private fun executeDepositHand(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().player?: return -1
        val hand = player.inventory.selectedItem

        if (hand.isEmpty) return sendError(player, "Hold a valid market item or use /deposit all.")
        val payout = MarketState.processItemDeposit(player, hand)
        if (payout <= 0) return sendError(player, "This item cannot be deposited into the market.")
        hand.count = 0
        return sendSuccess(player, String.format("Deposited items for $%.2f to your account.", payout))
    }

    private fun executeDepositAll(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().player?: return -1
	    if (player.level().dimension().identifier().namespace != "minecraft") return -1
        var totalPayout = .0

        for (i in 0..<player.inventory.containerSize) {
            val stack = player.inventory.getItem(i)
            if (stack.isEmpty) continue

            val payout = MarketState.processItemDeposit(player, stack)
            if (payout > 0) {
                totalPayout += payout
                player.inventory.removeItem(i, stack.count)
            }
        }

        if (totalPayout > 0) return sendSuccess(player, String.format("Deposited all valid items for $%.2f to your account.", totalPayout), 1)
        return sendError(player, "No valid market currency items found in inventory.", 0)
    }

    private fun executeNpcKill(ctx: CommandContext<CommandSourceStack>): Int {
        val level = ctx.getSource().level
        val id = StringArgumentType.getString(ctx, "id")

        if (NPCData.get()?.hasNpc(id) == true) {
            NPCData.get()?.removeNpc(id)
            NPCData.get()?.getMannequin(level, id)?.discard()
	        ctx.getSource().sendSuccess({ Component.literal("✔: Mannequin killed!").withStyle(ChatFormatting.GREEN) }, false)
	        return 1
        }
	    ctx.getSource().sendSuccess({ Component.literal("✖: Mannequin isn't alive!").withStyle(ChatFormatting.RED) }, false)
	    return 0
    }

    private fun executeNpcSetup(ctx: CommandContext<CommandSourceStack>): Int {
        val level = ctx.getSource().level
        val player = ctx.getSource().player?: return -1
        val id = StringArgumentType.getString(ctx, "id")

        if (NPCData.get()?.hasNpc(id) == true) return sendError(player, "Mannequin already exists!")
        NPCManager.spawn(id, level, BlockPos.containing(ctx.getSource().position))?: return sendError(player, "Mannequin already exists / id doesn't exist!")
        return sendSuccess(player, "Mannequin created successfully!")
    }

    private fun executeQuests(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().player?: return -1
        QuestManager.get()?.checkAndResetRotations(player)
        val activeQuests: MutableList<ActiveQuest> = QuestManager.getQuests(player).activeQuests
        player.sendSystemMessage(Component.literal("=== Active Quests ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
        if (activeQuests.isEmpty()) {
            player.sendSystemMessage(Component.literal(" You have no active quests.").withStyle(ChatFormatting.GRAY))
            return 0
        }

        for (activeQuest in activeQuests) {
            val quest: Quest = activeQuest.getQuest() ?: continue

            val categoryColor = when (quest.questType) {
                Quest.QuestCategory.DAILY -> ChatFormatting.YELLOW
                Quest.QuestCategory.WEEKLY -> ChatFormatting.LIGHT_PURPLE
                Quest.QuestCategory.NPC -> ChatFormatting.AQUA
            }

            val questLine = Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("[" + quest.questType.serializedName.uppercase(Locale.getDefault()) + "] ").withStyle(categoryColor, ChatFormatting.BOLD))
                .append(Component.literal(quest.title).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))

            if (activeQuest.isCompleted) {
                if (activeQuest.isClaimed) questLine.append(Component.literal("Completed").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC))
                else questLine.append(Component.literal("READY TO CLAIM!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            } else questLine.append(Component.literal(activeQuest.currentCount.toString() + "/" + quest.requiredCount).withStyle(ChatFormatting.AQUA))

            player.sendSystemMessage(questLine)
            if (quest.description.isNotEmpty()) player.sendSystemMessage(Component.literal("   " + quest.description).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
        }
        return 1
    }

	private fun executeMapArt(ctx: CommandContext<CommandSourceStack>): Int {
		val player = ctx.getSource().player?: return -1
		val url = StringArgumentType.getString(ctx, "url")

		if (!url.startsWith("http://") && !url.startsWith("https://")) return sendError(player, "Invalid URL! Must start with http:// or https://")
		CompletableFuture.runAsync {
			try {
				val img = ImageIO.read(URI(url).toURL())
				if (img == null) {
					SMPMod.minecraftServer?.execute {
						if (player.isRemoved) return@execute
						sendError(player, "Could not load image from the provided URL.")
					}
					return@runAsync
				}

				val mapW = max(1, img.width / 128)
				val mapH = max(1, img.height / 128)
				val cost = 300.0 * mapW * mapH

				SMPMod.minecraftServer?.execute {
					if (player.isRemoved) return@execute

					val eco = EconomyData.get() ?: return@execute
					if (eco.getBalance(player.uuid) < cost) {
						sendError<Int>(player, String.format("Insufficient funds! You need $%.2f for a %dx%d map.", cost, mapW, mapH))
						return@execute
					}

					if (eco.changeBalance(player.uuid, -cost)) {
						val sourceStack = player.createCommandSourceStack()
						val command = String.format("image2map create none %s", url)

						SMPMod.minecraftServer?.commands?.performPrefixedCommand(sourceStack, command)
						sendSuccess(player, String.format("Created a %dx%d map art for $%.2f!", mapW, mapH, cost))
					}
				}
			} catch (e: Exception) { SMPMod.minecraftServer?.execute { if (!player.isRemoved) sendError(player, "Failed to process image URL: " + e.message) }}
		}
		return 1
	}

    private fun executeSurface(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().player?: return -1
        player.teleportTo(player.x, ctx.source.level.getHeight(Heightmap.Types.WORLD_SURFACE, floor(player.x).toInt(), floor(player.z).toInt()).toDouble(), player.z)
        player.playSound(SoundEvents.WITHER_SHOOT, 3f, .5f)
        return 1
    }

    private fun executeEnderChest(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().player?: return -1
        player.openMenu(SimpleMenuProvider({ syncId, inventory, p -> ChestMenu.threeRows(syncId, inventory, p.getEnderChestInventory()) }, Component.translatable("block.minecraft.ender_chest")))
        player.awardStat(Stats.OPEN_ENDERCHEST, 1)
        return 1
    }

    private fun executeMarketAll(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.getSource().sendSuccess({ Component.literal("Market Prices").withStyle(ChatFormatting.GOLD) }, false)
	    (MarketState.state?: return -1).all.values.sortedBy { it.defaultPrice }.asReversed()
            .forEach {
                val ratio = (it.currentPrice / it.defaultPrice - 1) * 100.0

                val trend = if (ratio > 0) String.format(" (+%.1f%%)", ratio) else String.format(" (%.1f%%)", ratio)
                val trendColor = if (ratio >= 0) (if (ratio == .0) ChatFormatting.GRAY else ChatFormatting.RED) else ChatFormatting.GREEN

                val message = Component.literal("• ").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(it.mineral.getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(String.format(" | Buy: $%.2f | Sell: $%.2f", it.getBulkBuyCost(1), it.getBulkSellPayout(1))).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(trend).withStyle(trendColor))
                ctx.getSource().sendSuccess({ message }, false)
            }

        return 1
    }

    private fun executeMarketItem(ctx: CommandContext<CommandSourceStack>): Int {
        val targetItem = ItemArgument.getItem(ctx, "item").item().value()
        val market = MarketState.state?: return -1
        val data = market.get(targetItem) ?: return sendError(ctx.getSource().player?: return -1, "This item is not tracked by the market.", 0)

        ctx.getSource().sendSuccess({ Component.literal(String.format(" Base Price: $%.2f", data.defaultPrice)).withStyle(ChatFormatting.GRAY) }, false)
        ctx.getSource().sendSuccess({ Component.literal(String.format(" 1x   Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(1), data.getBulkSellPayout(1))).withStyle(ChatFormatting.WHITE) }, false)
        ctx.getSource().sendSuccess({ Component.literal(String.format(" 64x  Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(64), data.getBulkSellPayout(64))).withStyle(ChatFormatting.WHITE) }, false)
        return 1
    }

    private fun executeWithdraw(ctx: CommandContext<CommandSourceStack>, count: Int): Int {
        val item = ItemArgument.getItem(ctx, "item").item().value()
        val player = ctx.getSource().player?: return -1
	    if (player.level().dimension().identifier().namespace != "minecraft") return -1
        val totalCost = MarketState.buyMineral(player, item, count)
        if (totalCost == -2.0) {
            player.sendSystemMessage(Component.literal("✖: ").append(Component.translatable(item.getDescriptionId())).append(" is not a tradeable market item.").withStyle(ChatFormatting.RED))
            return -1
        } else if (totalCost == -1.0) {
            player.sendSystemMessage(Component.literal(String.format("✖: Insufficient balance. You need $%.2f to withdraw %dx ", (MarketState.state?: return -1).get(item)?.getBulkBuyCost(count) ?: .0, count)).append(Component.translatable(item.getDescriptionId())).append(".").withStyle(ChatFormatting.RED))
            return -1
        }

        giveExactItems(player, item, count)
        player.sendSystemMessage(Component.literal(String.format("🏢: Withdrew %dx ", count)).append(Component.translatable(item.getDescriptionId())).append(String.format(" for $%.2f.", totalCost)).withStyle(ChatFormatting.GREEN))
        return 1
    }

    private fun giveExactItems(player: ServerPlayer, item: Item, totalCount: Int) {
        var totalCount = totalCount
        val maxStack = item.defaultMaxStackSize
        while (totalCount > 0) {
            val stackSize = min(totalCount, maxStack)
            val stack = ItemStack(item, stackSize)
	        totalCount -= stackSize
            if (!player.inventory.add(stack)) player.drop(stack, false)?.setNoPickUpDelay()
        }
    }
}