package fun.spmc.smpmod.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fun.spmc.smpmod.discord.config.ConfigLoader;
import okhttp3.*;
import org.jspecify.annotations.NonNull;
import java.io.IOException;

import static fun.spmc.smpmod.SMPMod.modLogger;

public class DiscordWebhook {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    public static void sendChatMessage(String message, String playerName, String playerUUID) {
        String webhookUrl = ConfigLoader.CONFIG.webhook();
        if (webhookUrl.isEmpty()) return;

        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new JsonArray());

        JsonObject body = new JsonObject();
        body.addProperty("content", message);
        body.addProperty("username", playerName);
        body.addProperty("avatar_url", "https://mc-heads.net/head/" + playerUUID + "/512.png");
        body.add("allowed_mentions", allowedMentions);

        Request request = new Request.Builder().url(webhookUrl).post(RequestBody.create(body.toString(), MediaType.get("application/json"))).build();
        HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                modLogger.error("Failed to send Discord webhook message", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                response.close();
            }
        });
    }
}
