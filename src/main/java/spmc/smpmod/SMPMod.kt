package spmc.smpmod

import com.mojang.brigadier.CommandDispatcher
import spmc.smpmod.core.*
import spmc.smpmod.discord.*
import spmc.smpmod.discord.config.ConfigLoader
import spmc.smpmod.economy.EconomyData
import spmc.smpmod.economy.fluctuate.MarketState
import spmc.smpmod.economy.shop.ShopManager
import spmc.smpmod.fishing.mechanic.FishingManager
import spmc.smpmod.mobs.ServerMobEvents
import spmc.smpmod.mobs.boss.CrystalBoss
import spmc.smpmod.npc.NPCManager
import spmc.smpmod.quest.QuestManager
import spmc.smpmod.registry.*
import spmc.smpmod.treasure.*
import spmc.smpmod.utils.MessageUtils
import spmc.smpmod.vault.VaultData
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AfterDeath
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.ChatMessage
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt
import kotlin.system.exitProcess

@Environment(EnvType.SERVER)
class SMPMod : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        try {
            CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<CommandSourceStack>, context: CommandBuildContext, _: CommandSelection -> CommandRegistry.register(dispatcher, context) })
        } catch (e: Exception) {
            modLogger.error(ExceptionUtils.getStackTrace(e))
            exitProcess(1)
        }

        PolymerRegistry.init()

        ServerLifecycleEvents.SERVER_STARTED.register(ServerStarted { server: MinecraftServer ->
            try {
                ConfigLoader.checkConfigs()
                minecraftServer = server
                bot = JDABuilder.createDefault(ConfigLoader.CONFIG.token).setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(EventHandler()).enableIntents(
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES
                    ).build()
                bot?.awaitReady()
                messageChannel = bot?.getTextChannelById(ConfigLoader.CONFIG.messageChannelId)
                bot?.presence?.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Minecraft"))
                messageChannel?.sendMessage("Server has opened!")?.queue()
                bot?.updateCommands()?.addCommands(
                    Commands.slash("players", "Get the number of players."),
                    Commands.slash("market", "Get the market inside the server."),
                    Commands.slash("top", "Get the economy leaderboard.")
                        .addOption(OptionType.INTEGER, "page", "The leaderboard page number (defaults to 1)", false)
                )?.queue()

                FishingManager.register()
                MarketState.register()
                VaultData.register()
                NPCManager.register()
            } catch (e: Exception) {
                modLogger.error("Config not initialized, please finish the config.")
                throw RuntimeException(e)
            }
        })

        ShopManager.register()
        ChunkLoaderSavedData.register()
        ServerMobEvents.registerMobs()

        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler: ServerGamePacketListenerImpl, _: PacketSender, server: MinecraftServer ->
            val player = handler.getPlayer()
            BedrockSkinFetcher.restoreSkin(server, player)
            QuestManager.get()?.checkAndResetRotations(player)
            EconomyData.get().registerPlayer(player.getUUID(), player.gameProfile.name())
            player.awardRecipes(server.recipeManager.recipes.stream().distinct().filter { a: RecipeHolder<*> -> a.id().identifier().namespace == "smpmod" }.toList())
            if (messageChannel != null) messageChannel!!.sendMessage("[+] " + MarkdownSanitizer.escape(player.name.string)).queue()
        })

        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler: ServerGamePacketListenerImpl, _: MinecraftServer ->
            val player = handler.getPlayer()
            if (messageChannel != null) messageChannel?.sendMessage(
                "[-] " + MarkdownSanitizer.escape(
                    player.name.string
                )
            )?.queue()
        })

        ServerMessageEvents.CHAT_MESSAGE.register(ChatMessage { message: PlayerChatMessage, sender: ServerPlayer, _: ChatType.Bound ->
            DiscordWebhook.sendChatMessage(
                message.signedContent().replace("<[^>]*>".toRegex(), ""),
                sender.name.string,
                sender.getStringUUID()
            )
        })
        ServerLivingEntityEvents.AFTER_DEATH.register(AfterDeath { entity: LivingEntity, damageSource: DamageSource ->
            if (entity is ServerPlayer && messageChannel != null) {
                val deathMessage = damageSource.getLocalizedDeathMessage(entity).string
                val fullMessage = "☠ " + deathMessage + " at (" + entity.x.toInt() + ", " + entity.y
                    .toInt() + ", " + entity.z.toInt() + ")"
                messageChannel?.sendMessage(MarkdownSanitizer.escape(fullMessage))?.queue()

                val eco: EconomyData = EconomyData.get()
                val victimBalance: Double = eco.getBalance(entity.getUUID())

                if (victimBalance >= 1000) {
                    val lossPercent = .05 + (entity.getRandom().nextDouble() * .05)
                    val totalLost = ((victimBalance * lossPercent) * 100.0).roundToInt() / 100.0

                    if (totalLost > 0) {
                        eco.changeBalance(entity.getUUID(), -totalLost)
                        MessageUtils.sendError(entity, String.format("You died and lost $%.2f (%.1f%% of your balance)!", totalLost, lossPercent * 100), 0)

                        if (damageSource.entity is ServerPlayer && damageSource.entity?.getUUID() != entity.getUUID()) {
                            val killer: ServerPlayer = damageSource.entity as ServerPlayer
                            val bountyReward = ((totalLost * .7) * 100.0).roundToInt() / 100.0

                            eco.changeBalance(killer.getUUID(), bountyReward)
                            MessageUtils.sendSuccess(killer, String.format("⚔ You killed %s and claimed a $%.2f bounty!", entity.scoreboardName, bountyReward), 1)
                        }
                    }
                    TODO("create new bounty system")
                }
            }
        })

        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server: MinecraftServer ->
            if (server.playerList.players.isEmpty()) return@EndTick
            if (server.tickCount % 360 == 0) ShopManager.serverTickLoop(server)
            if (server.tickCount % 15 == 0) NPCManager.serverTickLoop(server)
            if (server.tickCount % 50 == 0) ChunkPool.serverTickLoop()
            if (server.tickCount % 1200 != 0) return@EndTick
            MarketState.serverTickLoop(server)

            val scoreboard: Scoreboard = server.scoreboard
            var objective = scoreboard.getObjective("play_time")
            if (objective == null) {
                objective = scoreboard.addObjective(
                    "play_time",
                    ObjectiveCriteria.DUMMY,
                    Component.literal("hours").withStyle(ChatFormatting.GOLD),
                    ObjectiveCriteria.RenderType.INTEGER,
                    false,
                    null
                )
                scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective)
            }
            for (player in server.playerList.players) {
                val playTime = player.stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
                if (playTime > 0) EconomyData.get().changeBalance(player.getUUID(), 1.2)

                val totalHours = playTime / 72000
                val scoreAccess = scoreboard.getOrCreatePlayerScore(player, objective)
                scoreAccess.set(totalHours)

                QuestManager.get()?.checkAndResetRotations(player)
            }
        })

        ServerLifecycleEvents.SERVER_STOPPED.register(ServerStopped { _: MinecraftServer ->
            messageChannel?.sendMessage("Server shutting down...")?.queue()
            bot?.shutdown()
        })

        PlayerBlockBreakEvents.AFTER.register(PlayerBlockBreakEvents.After { world: Level, player: Player, pos: BlockPos, state: BlockState, _: BlockEntity? -> TreasureHelper.onBlockBreak(world, player, pos, state) })
        ServerEntityEvents.ENTITY_LOAD.register(ServerEntityEvents.Load { entity: Entity, level: ServerLevel -> ServerMobEvents.onEntityJoin(entity, level) })
        UseBlockCallback.EVENT.register(UseBlockCallback { player: Player, world: Level, hand: InteractionHand, hitResult: BlockHitResult? -> CrystalBoss.eventSpawnBoss(player, world, hand, hitResult) })

        // proof of concept, TODO: make it better
        PlayerBlockBreakEvents.AFTER.register(PlayerBlockBreakEvents.After { world: Level, _: Player, pos: BlockPos, state: BlockState, _: BlockEntity? ->
            if (world.isClientSide) return@After
            if (state.`is`(Blocks.SHORT_GRASS) || state.`is`(Blocks.TALL_GRASS)) {
                TODO("to be fixed")
                //if (world.getRandom().nextFloat() < 0.08f) Block.popResource(world, pos, ItemStack(PlantRegistry.SEEDS.get("wheat")))
            }
        })
    }

    companion object {
        @JvmField val modLogger: Logger = LoggerFactory.getLogger("SMPMod")
        var bot: JDA? = null
        @JvmField var messageChannel: TextChannel? = null
        @JvmField var minecraftServer: MinecraftServer? = null
    }
}
