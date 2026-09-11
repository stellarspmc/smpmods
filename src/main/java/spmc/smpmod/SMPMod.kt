package spmc.smpmod

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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spmc.smpmod.core.BedrockSkinFetcher
import spmc.smpmod.core.ChunkLoaderSavedData
import spmc.smpmod.discord.DiscordWebhook
import spmc.smpmod.discord.EventHandler
import spmc.smpmod.discord.config.ConfigLoader
import spmc.smpmod.economy.EconomyData
import spmc.smpmod.economy.fluctuate.MarketState
import spmc.smpmod.economy.shop.ShopManager
import spmc.smpmod.fishing.mechanic.FishingManager
import spmc.smpmod.mobs.ServerMobEvents
import spmc.smpmod.mobs.boss.CrystalBoss
import spmc.smpmod.npc.NPCManager
import spmc.smpmod.quest.QuestManager
import spmc.smpmod.registry.CommandRegistry
import spmc.smpmod.registry.PolymerRegistry
import spmc.smpmod.mining.ChunkPool
import spmc.smpmod.mining.TreasureHelper
import spmc.smpmod.utils.MessageUtils
import spmc.smpmod.vault.VaultData
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt

@Environment(EnvType.SERVER)
class SMPMod : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        PolymerRegistry.init()

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
	        ConfigLoader.checkConfigs()
	        minecraftServer = server
	        bot = JDABuilder.createDefault(ConfigLoader.CONFIG?.token).setMemberCachePolicy(MemberCachePolicy.ALL).addEventListeners(EventHandler()).enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES).build()
	        CompletableFuture.runAsync {
		        try {
			        bot?.awaitReady()
			        messageChannel = ConfigLoader.CONFIG?.messageChannelId?.let { bot?.getTextChannelById(it) }
			        bot?.presence?.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Minecraft"))
			        messageChannel?.sendMessage("Server has opened!")?.queue()
			        bot?.updateCommands()?.addCommands(Commands.slash("players", "Get the number of players."), Commands.slash("market", "Get the market inside the server."), Commands.slash("top", "Get the economy leaderboard.").addOption(OptionType.INTEGER, "page", "The leaderboard page number (defaults to 1)", false))?.queue()
		        } catch (e: Exception) { modLogger.error("Failed to initialize Discord bot connection", e) }
	        }

	        FishingManager.register()
	        MarketState.register()
	        VaultData.register()
	        NPCManager.register()
	        ShopManager.register()
        }

	    ChunkLoaderSavedData.register()
        ServerMobEvents.registerMobs()

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
	        val player = handler.getPlayer()
	        BedrockSkinFetcher.restoreSkin(server, player)
	        QuestManager.get()?.checkAndResetRotations(player)
	        EconomyData.get()?.registerPlayer(player.getUUID(), player.gameProfile.name())
	        player.awardRecipes(server.recipeManager.recipes.distinct().filter { it.id().identifier().namespace == "smpmod" })
	        messageChannel?.sendMessage("[+] " + MarkdownSanitizer.escape(player.name.string))?.queue()
        }


	    ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
	        if (entity is ServerPlayer && messageChannel != null) {
		        if (entity.level().dimension().identifier().namespace != "minecraft") return@register
		        messageChannel?.sendMessage(MarkdownSanitizer.escape("☠ " + damageSource.getLocalizedDeathMessage(entity).string + " at (" + entity.x.toInt() + ", " + entity.y.toInt() + ", " + entity.z.toInt() + ")"))?.queue()
		        val eco = EconomyData.get() ?: return@register
		        val victimBalance = eco.getBalance(entity.getUUID())

		        if (victimBalance >= 1000) {
			        val lossPercent = .05 + (entity.getRandom().nextDouble() * .05)
			        val totalLost = ((victimBalance * lossPercent) * 100.0).roundToInt() / 100.0

			        if (totalLost > 0) {
				        eco.changeBalance(entity.getUUID(), -totalLost)
				        MessageUtils.sendError<Int>(entity, String.format("You died and lost $%.2f (%.1f%% of your balance)!", totalLost, lossPercent * 100))

				        if (damageSource.entity?.getUUID() != entity.getUUID()) {
					        val killer = damageSource.entity as? ServerPlayer?: return@register
					        val bountyReward = ((totalLost * .7) * 100.0).roundToInt() / 100.0

					        eco.changeBalance(killer.getUUID(), bountyReward)
					        MessageUtils.sendSuccess<Int>(killer, String.format("⚔ You killed %s and claimed a $%.2f bounty!", entity.scoreboardName, bountyReward))
				        }
			        }// TODO: create new bounty system
		        }
	        }
        }

	    ServerTickEvents.END_SERVER_TICK.register {
		    if (it.playerList.players.isEmpty()) return@register
		    if (it.tickCount % 360 == 0) ShopManager.serverTickLoop(it)
		    if (it.tickCount % 15 == 0) NPCManager.serverTickLoop(it)
		    if (it.tickCount % 50 == 0) ChunkPool.serverTickLoop()
		    if (it.tickCount % 1200 != 0) return@register
		    MarketState.serverTickLoop(it)

		    val scoreboard = it.scoreboard
		    val objective = scoreboard.getObjective("play_time") ?: scoreboard.addObjective("play_time", ObjectiveCriteria.DUMMY, Component.literal("hours").withStyle(ChatFormatting.GOLD), ObjectiveCriteria.RenderType.INTEGER, false, null)
		    scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective)
		    for (player in it.playerList.players) {
			    val playTime = player.stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
			    if (playTime > 0) EconomyData.get()?.changeBalance(player.getUUID(), 1.2)

			    scoreboard.getOrCreatePlayerScore(player, objective).set(playTime / 72000)
			    QuestManager.get()?.checkAndResetRotations(player)
		    }
	    }

	    ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> messageChannel?.sendMessage("[-] " + MarkdownSanitizer.escape(handler.getPlayer().name.string))?.queue() }
	    ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ -> DiscordWebhook.sendChatMessage(message.signedContent().replace("<[^>]*>".toRegex(), ""), sender.name.string, sender.getStringUUID()) } // TODO: diff between creative and survival
	    ServerLifecycleEvents.SERVER_STOPPED.register { messageChannel?.sendMessage("Server shutting down...")?.queue(); bot?.shutdown() }
	    PlayerBlockBreakEvents.AFTER.register(TreasureHelper::onBlockBreak)
	    ServerEntityEvents.ENTITY_LOAD.register(ServerMobEvents::onEntityJoin)
	    UseBlockCallback.EVENT.register(CrystalBoss::eventSpawnBoss) // TODO: better handling
	    CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, context, _ -> CommandRegistry.register(dispatcher, context) })

	    // proof of concept, TODO: make it better
        PlayerBlockBreakEvents.AFTER.register { world, _, _, state, _ ->
	        if (world.isClientSide) return@register
	        if (world.dimension().identifier().namespace != "minecraft") return@register
	        if (state.`is`(Blocks.SHORT_GRASS) || state.`is`(Blocks.TALL_GRASS)) {
		        //if (world.getRandom().nextFloat() < 0.08f) Block.popResource(world, pos, ItemStack(PlantRegistry.SEEDS.get("wheat"))) TODO: to be fixed
	        }
        }
    }

    companion object {
        @JvmField val modLogger: Logger = LoggerFactory.getLogger("SMPMod")
        var bot: JDA? = null
        @JvmField var messageChannel: TextChannel? = null
        @JvmField var minecraftServer: MinecraftServer? = null
    }
}
