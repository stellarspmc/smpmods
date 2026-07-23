package fun.spmc.smpmod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import fun.spmc.smpmod.discord.EventHandler;
import fun.spmc.smpmod.minecraft.chunk.ChunkLoaderHandler;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import fun.spmc.smpmod.minecraft.economy.shop.ShopInteractionHandler;
import fun.spmc.smpmod.minecraft.treasure.TreasureEvents;
import fun.spmc.smpmod.minecraft.utils.CommandRegistry;
import fun.spmc.smpmod.minecraft.MobSpawnedEvent;

import fun.spmc.smpmod.discord.utils.ConfigLoader;

import fun.spmc.smpmod.minecraft.utils.GeyserMCFix;
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
import okhttp3.*;

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
                bot = JDABuilder.createDefault(ConfigLoader.BOT_TOKEN)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
                        .addEventListeners(new EventHandler())
                        .build();
                bot.awaitReady();
                messageChannel = bot.getTextChannelById(ConfigLoader.MESSAGE_CHANNEL_ID);
                bot.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Minecraft"));
                messageChannel.sendMessage("Server has opened!").queue();

                bot.updateCommands().addCommands(
                        Commands.slash("players", "Get the number of players."),
                        Commands.slash("top", "Get the economy leaderboard.")
                                .addOption(OptionType.INTEGER, "page", "The leaderboard page number (defaults to 1)", false)
                ).queue();

            } catch (Exception e) {
                modLogger.error("Put Information into the Config");
                throw new RuntimeException(e);
            }
        });

        ShopInteractionHandler.register();
        ChunkLoaderHandler.register();

        ServerPlayConnectionEvents.JOIN.register((handler, _, server) -> {
            ServerPlayer player = handler.getPlayer();
            GeyserMCFix.restoreSkin(server, player);
            EconomySavedData eco = EconomySavedData.get(player.level());
            eco.registerPlayer(player.getUUID(), player.getGameProfile().name());

            if (messageChannel != null) messageChannel.sendMessage("[+] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> {
            ServerPlayer player = handler.getPlayer();
            if (messageChannel != null) messageChannel.sendMessage("[-] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, _) -> sendWebhookMessage(message.signedContent(), sender.getName().getString(), sender.getStringUUID()));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player && messageChannel != null) {
                String deathMessage = damageSource.getLocalizedDeathMessage(player).getString();
                String fullMessage = "☠ " + deathMessage + " (at " + (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ() + ")";
                messageChannel.sendMessage(MarkdownSanitizer.escape(fullMessage)).queue();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
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
                    EconomySavedData eco = EconomySavedData.get(player.level());
                    eco.changeBalance(player.getUUID(), 1.2);
                }

                int totalHours = playTime / 72000;
                ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(player, objective);
                scoreAccess.set(totalHours);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((_) -> bot.shutdownNow());
        PlayerBlockBreakEvents.AFTER.register(TreasureEvents::onBlockBreak);
        ServerEntityEvents.ENTITY_LOAD.register(MobSpawnedEvent::onEntityJoin);
    }

    public static void sendWebhookMessage(String message, String playerName, String playerUUID) {
        JsonObject body = new JsonObject();
        body.addProperty("content", message);
        body.addProperty("username", playerName);
        body.addProperty("avatar_url", "https://mc-heads.net/head/%player%/512.png".replace("%player%", playerUUID));
        body.add("allowed_mentions", new Gson().fromJson("{\"parse\":[]}", JsonObject.class));
        Request request = new Request.Builder()
                .url(ConfigLoader.WEBHOOK_URL)
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            response.close();
        } catch (Exception e) {
            modLogger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
