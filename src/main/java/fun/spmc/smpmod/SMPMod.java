package fun.spmc.smpmod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import fun.spmc.smpmod.discord.EventHandler;
import fun.spmc.smpmod.minecraft.utils.CommandRegistry;
import fun.spmc.smpmod.minecraft.events.BlockBrokenEvent;
import fun.spmc.smpmod.minecraft.events.MobSpawnedEvent;

import fun.spmc.smpmod.discord.utils.ConfigLoader;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.util.ActionResult;
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

    @Override
    public void onInitializeServer() {
        try {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandRegistry.register(dispatcher));
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

            } catch (Exception e) {
                modLogger.error("Put Information into the Config");
                throw new RuntimeException(e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> bot.shutdownNow());

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity livingEntity) {
                if (player.getRandom().nextDouble() <= (0.15))
                    livingEntity.setHealth(livingEntity.getHealth());
            }
            return ActionResult.PASS;
        });

        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerWorld world) -> MobSpawnedEvent.onEntityJoin(entity));

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> BlockBrokenEvent.onBreakBlock((ServerWorld) world, player, state, pos));
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
