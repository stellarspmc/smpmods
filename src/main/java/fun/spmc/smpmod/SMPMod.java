package fun.spmc.smpmod;

import fun.spmc.smpmod.discord.DiscordWebhook;
import fun.spmc.smpmod.discord.EventHandler;
import fun.spmc.smpmod.chunk.ChunkLoaderSavedData;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.economy.fluctuate.MarketState;
import fun.spmc.smpmod.economy.shop.ShopManager;
import fun.spmc.smpmod.events.ServerMobSpawner;
import fun.spmc.smpmod.fishing.mechanic.FishingLoot;
import fun.spmc.smpmod.fishing.mechanic.FishingManager;
import fun.spmc.smpmod.treasure.TreasureEvents;
import fun.spmc.smpmod.utils.CommandRegistry;
import fun.spmc.smpmod.discord.config.ConfigLoader;
import fun.spmc.smpmod.bedrock.BedrockSkinFetcher;

import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.vault.VaultData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.SERVER)
public class SMPMod implements DedicatedServerModInitializer {
    public static final Logger modLogger = LoggerFactory.getLogger("SMPMod");
    public static JDA bot;
    public static TextChannel messageChannel;
    public static MinecraftServer minecraftServer;
    private int tickCounter = 0;

    @Override
    public void onInitializeServer() {
        try {
            CommandRegistrationCallback.EVENT.register(CommandRegistry::register);
        } catch (Exception e) {
            modLogger.error(ExceptionUtils.getStackTrace(e));
            System.exit(1);
        }

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            try {
                ConfigLoader.checkConfigs();
                minecraftServer = server;
                bot = JDABuilder.createDefault(ConfigLoader.CONFIG.token()).setMemberCachePolicy(MemberCachePolicy.ALL).addEventListeners(new EventHandler()).enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES).build();
                bot.awaitReady();
                messageChannel = bot.getTextChannelById(ConfigLoader.CONFIG.messageChannelId());
                bot.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Minecraft"));
                messageChannel.sendMessage("Server has opened!").queue();
                bot.updateCommands().addCommands(
                        Commands.slash("players", "Get the number of players."),
                        Commands.slash("market", "Get the market inside the server."),
                        Commands.slash("top", "Get the economy leaderboard.").addOption(OptionType.INTEGER, "page", "The leaderboard page number (defaults to 1)", false)
                ).queue();

                FishingManager.register();
                MarketState.register();
                VaultData.register();
            } catch (Exception e) {
                modLogger.error("Config not initialized, please finish the config.");
                throw new RuntimeException(e);
            }
        });

        SMPItems.register();
        ShopManager.register();
        ChunkLoaderSavedData.register();
        ServerMobSpawner.registerMobs();

        ServerPlayConnectionEvents.JOIN.register((handler, _, server) -> {
            ServerPlayer player = handler.getPlayer();
            BedrockSkinFetcher.restoreSkin(server, player);
            EconomyData eco = EconomyData.get();
            eco.registerPlayer(player.getUUID(), player.getGameProfile().name());

            if (messageChannel != null) messageChannel.sendMessage("[+] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> {
            ServerPlayer player = handler.getPlayer();
            if (messageChannel != null) messageChannel.sendMessage("[-] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, _) -> DiscordWebhook.sendChatMessage(message.signedContent().replaceAll("<[^>]*>", ""), sender.getName().getString(), sender.getStringUUID()));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player && messageChannel != null) {
                String deathMessage = damageSource.getLocalizedDeathMessage(player).getString();
                String fullMessage = "☠ " + deathMessage + " at (" + (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ() + ")";
                messageChannel.sendMessage(MarkdownSanitizer.escape(fullMessage)).queue();

                EconomyData eco = EconomyData.get();
                double victimBalance = eco.getBalance(player.getUUID());

                if (victimBalance >= 1000) {
                    double lossPercent = .05 + (player.getRandom().nextDouble() * .05);
                    double totalLost = Math.round((victimBalance * lossPercent) * 100.0) / 100.0;

                    if (totalLost > 0) {
                        eco.changeBalance(player.getUUID(), -totalLost);
                        MessageUtils.sendErrorMessage(player, String.format("You died and lost $%.2f (%.1f%% of your balance)!", totalLost, lossPercent * 100));

                        if (damageSource.getEntity() instanceof ServerPlayer killer && !killer.getUUID().equals(player.getUUID())) {
                            double bountyReward = Math.round((totalLost * .7) * 100.0) / 100.0;

                            eco.changeBalance(killer.getUUID(), bountyReward);
                            MessageUtils.sendSuccessMessage(killer, String.format("⚔ You killed %s and claimed a $%.2f bounty!", player.getScoreboardName(), bountyReward));
                        }
                    }
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getPlayerList().getPlayers().isEmpty()) return;
            tickCounter++;

            if (tickCounter % 1200 != 0) return;
            Scoreboard scoreboard = server.getScoreboard();

            Objective objective = scoreboard.getObjective("play_time");
            if (objective == null) {
                objective = scoreboard.addObjective(
                        "play_time",
                        ObjectiveCriteria.DUMMY,
                        Component.literal("hours").withStyle(ChatFormatting.GOLD),
                        ObjectiveCriteria.RenderType.INTEGER,
                        false,
                        null
                );
                scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                int playTime = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
                if (playTime > 0) {
                    EconomyData eco = EconomyData.get();
                    eco.changeBalance(player.getUUID(), 1.2);
                }

                int totalHours = playTime / 72000;
                ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(player, objective);
                scoreAccess.set(totalHours);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((_) -> {
            messageChannel.sendMessage("Server shutting down...").queue();
            bot.shutdown();
        });

        PlayerBlockBreakEvents.AFTER.register(TreasureEvents::onBlockBreak);
        ServerEntityEvents.ENTITY_LOAD.register(ServerMobSpawner::onEntityJoin);
    }
}
