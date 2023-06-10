package ml.spmc.smpmod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ml.spmc.smpmod.minecraft.command.AllCommands;
import ml.spmc.smpmod.utils.ConfigLoader;
import ml.spmc.smpmod.music.MusicPlayer;
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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Environment(EnvType.SERVER)
public class SMPMod implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("SMPMod");
    public static net.dv8tion.jda.api.JDA JDA;
    public static TextChannel MESSAGECHANNEL;
    public static MinecraftServer SERVER;

    @Override
    public void onInitializeServer() {
        try {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> AllCommands.register(dispatcher));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            System.exit(1);
        }
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            try {
                ConfigLoader.checkConfigs();

                SERVER = server;
                JDA = JDABuilder.createDefault(ConfigLoader.BOT_TOKEN).setHttpClient(new OkHttpClient.Builder()
                                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                                .build())
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
                        .addEventListeners(new EventHandler())
                        .build();
                JDA.awaitReady();
                MESSAGECHANNEL = JDA.getTextChannelById(ConfigLoader.MESSAGE_CHANNEL_ID);
                JDA.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Minecraft"));
                String topic = "SMP Opened!";
                MESSAGECHANNEL.getManager().setTopic(topic).queue();
                MusicPlayer.playMusic();
                MESSAGECHANNEL.sendMessage("<@&964807039702421564> Server has opened!").queue();
                
            } catch (Exception e) {
                LOGGER.error("PLEASE PUT INFORMATION INTO CONFIG");
                throw new RuntimeException(e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            MESSAGECHANNEL.sendMessage("Server has stopped!").queue();
            String topic = "Oh no! Server closed!";
            MESSAGECHANNEL.getManager().setTopic(topic);
            JDA.shutdownNow();
        });
    }

    public static void sendWebhookMessage(String message, String playername, String playeruuid) {
        JsonObject body = new JsonObject();
        body.addProperty("content", message);
        body.addProperty("username", playername);
        body.addProperty("avatar_url", ConfigLoader.AVATAR_URL.replace("%player%", playeruuid));
        body.add("allowed_mentions", new Gson().fromJson("{\"parse\":[]}", JsonObject.class));

        Request request = new Request.Builder()
                .url(ConfigLoader.WEBHOOK_URL)
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            response.close();
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
