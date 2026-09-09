package spmc.smpmod.registry

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.EconomyData
import spmc.smpmod.economy.fluctuate.FluctuationData
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
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.server.permissions.PermissionSet
import net.minecraft.server.players.NameAndId
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.levelgen.Heightmap
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream
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
            .executes { ctx: CommandContext<CommandSourceStack> -> executeTop(ctx, 1) }
            .then(Commands.argument<Int>("page", IntegerArgumentType.integer(1))
                .executes { ctx: CommandContext<CommandSourceStack> -> executeTop(ctx, IntegerArgumentType.getInteger(ctx, "page")) }))

        dispatcher.register(Commands.literal("send")
            .then(Commands.argument<GameProfileArgument.Result>("player", GameProfileArgument.gameProfile())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                    .executes { ctx: CommandContext<CommandSourceStack> -> executeSend(ctx) })))

        dispatcher.register(Commands.literal("deposit")
            .executes { ctx: CommandContext<CommandSourceStack> -> executeDepositHand(ctx) }
            .then(Commands.literal("all")
                .executes { ctx: CommandContext<CommandSourceStack> -> executeDepositAll(ctx) }))

        dispatcher.register(Commands.literal("market")
            .executes { ctx: CommandContext<CommandSourceStack> -> executeMarketAll(ctx) }
            .then(Commands.argument("item", ItemArgument.item(context)).suggests(streamToSuggestion(MarketState.getState().getAll().keys.stream())))
            .executes { ctx: CommandContext<CommandSourceStack> -> executeMarketItem(ctx) })

        dispatcher.register(Commands.literal("withdraw")
            .then(Commands.argument<ItemInput>("item", ItemArgument.item(context))
                .suggests(streamToSuggestion(Stream.concat(MarketState.getState().getAll().keys.stream(), Stream.of(Items.DIAMOND))))
                .executes { ctx: CommandContext<CommandSourceStack> -> executeWithdraw(ctx, 1) }
                .then(Commands.argument<Int>("count", IntegerArgumentType.integer(1))
                    .executes { ctx: CommandContext<CommandSourceStack> -> executeWithdraw(ctx, IntegerArgumentType.getInteger(ctx, "count")) })))

        dispatcher.register(Commands.literal("mapart")
            .then(Commands.argument<String>("url", StringArgumentType.greedyString())
                .executes { ctx: CommandContext<CommandSourceStack> -> executeMapArt(ctx) }))

        dispatcher.register(Commands.literal("npc")
            .then(Commands.literal("kill")
                .then(Commands.argument<String>("id", StringArgumentType.greedyString())
                    .requires { source: CommandSourceStack -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS) }
                    .suggests { _: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder -> SharedSuggestionProvider.suggest(NPCManager.allIds, builder) }
                    .executes { ctx: CommandContext<CommandSourceStack> -> executeNpcKill(ctx) }))
            .then(Commands.literal("setup")
                .then(Commands.argument<String>("id", StringArgumentType.greedyString())
                    .requires { source: CommandSourceStack -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS) }
                    .suggests { _: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder -> SharedSuggestionProvider.suggest(NPCManager.allIds, builder) }
                    .executes { ctx: CommandContext<CommandSourceStack> -> executeNpcSetup(ctx) })))

        dispatcher.register(Commands.literal("fishing").executes { ctx: CommandContext<CommandSourceStack> -> FishTracker.openFishIndexMenu(ctx.getSource().playerOrException) })
        dispatcher.register(Commands.literal("vault").executes { ctx: CommandContext<CommandSourceStack> -> VaultData.sendVaultMessage(ctx.getSource().playerOrException) })
        dispatcher.register(Commands.literal("quests").executes { ctx: CommandContext<CommandSourceStack> -> executeQuests(ctx) })
        dispatcher.register(Commands.literal("surface").executes { ctx: CommandContext<CommandSourceStack> -> executeSurface(ctx) })
        dispatcher.register(Commands.literal("enderchest") .executes { ctx: CommandContext<CommandSourceStack> -> executeEnderChest(ctx) })
    }

    private fun buildBalanceNode(name: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { ctx: CommandContext<CommandSourceStack> -> executeBalance(ctx, NameAndId(ctx.getSource().playerOrException.gameProfile)) }
            .then(Commands.argument<GameProfileArgument.Result>("player", GameProfileArgument.gameProfile())
            .executes { ctx: CommandContext<CommandSourceStack> -> executeBalance(ctx, GameProfileArgument.getGameProfiles(ctx, "player").iterator().next()) })
    }

    private fun executeBalance(ctx: CommandContext<CommandSourceStack>, target: NameAndId): Int {
        val eco: EconomyData = EconomyData.get()
        ctx.getSource().sendSuccess({ Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(target.name() + " has ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", eco.getBalance(target.id()))).withStyle(ChatFormatting.RED))
                .append(Component.literal(".").withStyle(ChatFormatting.GOLD))
        }, false)
        return 1
    }

    private fun executeTop(ctx: CommandContext<CommandSourceStack>, page: Int): Int {
        val eco: EconomyData = EconomyData.get()
        ctx.getSource().sendSuccess({ Component.literal("Wealth Leaderboard").withStyle(ChatFormatting.GOLD).append("\n").append(eco.getMinecraftTop(page)) }, false)
        return 1
    }

    @Throws(CommandSyntaxException::class)
    private fun executeSend(ctx: CommandContext<CommandSourceStack>): Int {
        val target = GameProfileArgument.getGameProfiles(ctx, "player").iterator().next()
        val sender = ctx.getSource().playerOrException
        val amount = ((DoubleArgumentType.getDouble(ctx, "amount") * 100f).roundToInt() / 100f).toDouble()
        if (sender.getUUID() == target.id()) return sendError(sender, "You cannot send money to yourself.", 0)

        val eco: EconomyData = EconomyData.get()
        if (eco.changeBalance(sender.getUUID(), -amount)) {
            eco.changeBalance(target.id(), amount)

            SMPMod.minecraftServer?.playerList?.getPlayer(target.id())?.sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(sender.name.string).withStyle(ChatFormatting.RED))
            )

            return sendSuccess(sender, String.format("Sent $%.2f to %s.", amount, target.name()), 1)
        }

        return sendError(sender, "Insufficient funds.", 0)
    }

    @Throws(CommandSyntaxException::class)
    private fun executeDepositHand(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().playerOrException
        val hand = player.inventory.selectedItem

        if (hand.isEmpty) return sendError(player, "Hold a valid market item or use /deposit all.", 0)
        val payout = MarketState.processItemDeposit(player, hand)
        if (payout <= 0) return sendError(player, "This item cannot be deposited into the market.", 0)
        hand.count = 0
        return sendSuccess(player, String.format("Deposited items for $%.2f to your account.", payout), 1)
    }

    @Throws(CommandSyntaxException::class)
    private fun executeDepositAll(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().playerOrException
        var totalPayout = 0.0

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

    @Throws(CommandSyntaxException::class)
    private fun executeNpcKill(ctx: CommandContext<CommandSourceStack>): Int {
        val level = ctx.getSource().level
        val id = StringArgumentType.getString(ctx, "id")

        if (NPCData.get()?.hasNpc(id) == true) {
            NPCData.get()?.removeNpc(id)
            NPCData.get()?.getMannequin(level, id)?.discard()
            return sendSuccess(ctx.getSource().playerOrException, "Mannequin killed!", 1)
        }
        return sendError(ctx.getSource().playerOrException, "Mannequin isn't alive!", 0)
    }

    @Throws(CommandSyntaxException::class)
    private fun executeNpcSetup(ctx: CommandContext<CommandSourceStack>): Int {
        val level = ctx.getSource().level
        val pos = ctx.getSource().position
        val id = StringArgumentType.getString(ctx, "id")

        if (NPCData.get()?.hasNpc(id) == true) return sendError(ctx.getSource().playerOrException, "Mannequin already exists!", 0)
        NPCManager.spawn(id, level, BlockPos.containing(pos))?: return sendError(ctx.getSource().playerOrException, "Mannequin already exists / id doesn't exist!", 0)
        return sendSuccess(ctx.getSource().playerOrException, "Mannequin created successfully!", 1)
    }

    @Throws(CommandSyntaxException::class)
    private fun executeQuests(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().playerOrException
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
        val player = ctx.getSource().playerOrException
        val url = StringArgumentType.getString(ctx, "url")

        if (!url.startsWith("http://") && !url.startsWith("https://")) return sendError(player, "Invalid URL! Must start with http:// or https://", 0)
        CompletableFuture.runAsync {
            try {
                val imageUrl = URI(url).toURL()
                val img = ImageIO.read(imageUrl)

                if (img == null) {
                    sendError(player, "Could not load image from the provided URL.", 0)
                    return@runAsync
                }

                val mapW = max(1, img.width / 128)
                val mapH = max(1, img.height / 128)
                val cost = (300 * mapW * mapH).toDouble()

                SMPMod.minecraftServer?.execute {
                    val eco: EconomyData = EconomyData.get()
                    if (eco.getBalance(player.getUUID()) < cost) {
                        sendError(player, String.format("Insufficient funds! You need $%.2f for a %dx%d map.", cost, mapW, mapH), 0)
                        return@execute
                    }
                    if (eco.changeBalance(player.getUUID(), -cost)) {
                        SMPMod.minecraftServer?.commands?.performPrefixedCommand(player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS), String.format("image2map create %s %s", "none", url))
                        sendSuccess(player, String.format("Created a %dx%d map art for $%.2f!", mapW, mapH, cost), 1)
                    }
                }
            } catch (e: Exception) {
                sendError(player, "Failed to process image URL: " + e.message, 0)
            }
        }
        return 1
    }

    @Throws(CommandSyntaxException::class)
    private fun executeSurface(ctx: CommandContext<CommandSourceStack>): Int {
        val player: Player = ctx.getSource().playerOrException
        player.teleportTo(player.x, ctx.source.level.getHeight(Heightmap.Types.WORLD_SURFACE, floor(player.x).toInt(), floor(player.z).toInt()).toDouble(), player.z)
        player.playSound(SoundEvents.WITHER_SHOOT, 3f, .5f)
        return 1
    }

    @Throws(CommandSyntaxException::class)
    private fun executeEnderChest(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getSource().playerOrException
        player.openMenu(SimpleMenuProvider({ syncId: Int, inventory: Inventory, p: Player -> ChestMenu.threeRows(syncId, inventory, p.getEnderChestInventory()) }, Component.translatable("block.minecraft.ender_chest")))
        player.awardStat(Stats.OPEN_ENDERCHEST, 1)
        return 1
    }

    private fun executeMarketAll(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.getSource().sendSuccess({ Component.literal("Market Prices").withStyle(ChatFormatting.GOLD) }, false)
        MarketState.getState().getAll().values.stream()
            .sorted { e1: FluctuationData, e2: FluctuationData -> e2.getDefaultPrice().compareTo(e1.getDefaultPrice()) }
            .forEach { data: FluctuationData ->
                val buyUnit: Double = data.getBulkBuyCost(1)
                val sellUnit: Double = data.getBulkSellPayout(1)
                val ratio: Double = (data.currentPrice / data.getDefaultPrice() - 1) * 100.0

                val trend = if (ratio > 0) String.format(" (+%.1f%%)", ratio) else String.format(" (%.1f%%)", ratio)
                val trendColor = if (ratio >= 0) (if (ratio == 0.0) ChatFormatting.GRAY else ChatFormatting.RED) else ChatFormatting.GREEN

                val message: Component = Component.literal("• ").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(data.mineral.getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(String.format(" | Buy: $%.2f | Sell: $%.2f", buyUnit, sellUnit)).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(trend).withStyle(trendColor))
                ctx.getSource().sendSuccess({ message }, false)
            }

        return 1
    }

    @Throws(CommandSyntaxException::class)
    private fun executeMarketItem(ctx: CommandContext<CommandSourceStack>): Int {
        val targetItem = ItemArgument.getItem(ctx, "item").item().value()
        val market: MarketState = MarketState.getState()

        val data: FluctuationData = market.get(targetItem) ?: return sendError(ctx.getSource().playerOrException, "This item is not tracked by the market.", 0)

        ctx.getSource().sendSuccess({ Component.literal(String.format(" Base Price: $%.2f", data.getDefaultPrice())).withStyle(ChatFormatting.GRAY) }, false)
        ctx.getSource().sendSuccess({ Component.literal(String.format(" 1x   Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(1), data.getBulkSellPayout(1))).withStyle(ChatFormatting.WHITE) }, false)
        ctx.getSource().sendSuccess({ Component.literal(String.format(" 64x  Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(64), data.getBulkSellPayout(64))).withStyle(ChatFormatting.WHITE) }, false)
        return 1
    }

    @Throws(CommandSyntaxException::class)
    private fun executeWithdraw(ctx: CommandContext<CommandSourceStack>, count: Int): Int {
        val item = ItemArgument.getItem(ctx, "item").item().value()
        val player = ctx.getSource().playerOrException
        val totalCost: Double = MarketState.buyMineral(player, item, count)
        if (totalCost == -2.0) {
            player.sendSystemMessage(Component.literal("✖: ").append(Component.translatable(item.getDescriptionId())).append(" is not a tradeable market item.").withStyle(ChatFormatting.RED))
            return -1
        } else if (totalCost == -1.0) {
            player.sendSystemMessage(Component.literal(String.format("✖: Insufficient balance. You need $%.2f to withdraw %dx ", MarketState.getState().get(item)?.getBulkBuyCost(count) ?: 0.0, count)).append(Component.translatable(item.getDescriptionId())).append(".").withStyle(ChatFormatting.RED))
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
            if (!player.inventory.add(stack)) {
                player.drop(stack, false)?.setNoPickUpDelay()
                totalCount -= stackSize
            }
        }
    }
}