package fun.spmc.smpmod.minecraft.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static fun.spmc.smpmod.SMPMod.modLogger;

public class GeyserMCFix {
    private static final Duration SKIN_REQUEST_TIMEOUT = Duration.ofSeconds(8L);
    private static final int MAX_FETCH_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 850L;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void restoreSkin(MinecraftServer server, ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(uuid);
        if (floodgatePlayer == null) return;

        String xuid = floodgatePlayer.getXuid();
        fetchAndApplySkin(server, uuid, player.getGameProfile().name(), xuid, 0);
    }

    private static void fetchAndApplySkin(MinecraftServer server, UUID playerId, String playerName, String xuid, int attempt) {
        GeyserSkinClient.fetchSkin(xuid).thenAccept(skinOpt -> {
            if (skinOpt.isEmpty()) scheduleRetry(server, playerId, playerName, xuid, attempt);
            else server.execute(() -> applySkin(server, playerId, skinOpt.get()));
        }).exceptionally(_ -> {
            modLogger.warn("Failed to fetch Bedrock skin for {} ({}), retrying...", playerName, xuid);
            scheduleRetry(server, playerId, playerName, xuid, attempt);
            return null;
        });
    }

    private static void scheduleRetry(MinecraftServer server, UUID playerId, String playerName, String xuid, int attempt) {
        if (attempt >= MAX_FETCH_RETRIES) {
            modLogger.info("No converted Bedrock skin available for {} after max attempts.", playerName);
            return;
        }

        scheduler.schedule(() -> server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null && Objects.requireNonNull(player.connection).isAcceptingMessages()) fetchAndApplySkin(server, playerId, playerName, xuid, attempt + 1);
        }), RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private static void applySkin(MinecraftServer server, UUID playerId, SkinProperty skin) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) return;

        GameProfile profile = player.getGameProfile();

        profile.properties().removeAll("textures");
        profile.properties().put("textures", new Property("textures", skin.value(), skin.signature()));

        resyncPlayerSkinToClients(server, player);
        modLogger.info("Successfully restored Bedrock skin for {}", player.getScoreboardName());
    }

    private static void resyncPlayerSkinToClients(MinecraftServer server, ServerPlayer player) {
        ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));
        ClientboundPlayerInfoUpdatePacket addPacket = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                List.of(player)
        );

        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            other.connection.send(removePacket);
            other.connection.send(addPacket);
        }
    }

    private record SkinProperty(String value, String signature) {}

    // --- HTTP Geyser API Client ---
    private static class GeyserSkinClient {
        private static final Gson GSON = new Gson();
        private static final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(SKIN_REQUEST_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        public static CompletableFuture<Optional<SkinProperty>> fetchSkin(String xuid) {
            if (xuid == null || xuid.isBlank()) return CompletableFuture.completedFuture(Optional.empty());

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.geysermc.org/v2/skin/" + xuid))
                    .timeout(SKIN_REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(GeyserSkinClient::parseSkinResponse);
        }

        private static Optional<SkinProperty> parseSkinResponse(HttpResponse<String> response) {
            int statusCode = response.statusCode();
            if (statusCode == 404) return Optional.empty();
            else if (statusCode >= 200 && statusCode < 300) {
                try (Reader reader = new StringReader(response.body())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!json.has("value")) return Optional.empty();

                    String value = GSON.fromJson(json.get("value"), String.class);
                    String signature = json.has("signature") ? GSON.fromJson(json.get("signature"), String.class) : null;

                    if (value == null || value.isBlank()) return Optional.empty();
                    return Optional.of(new SkinProperty(value, signature));
                } catch (IllegalStateException | IOException error) {
                    throw new IllegalArgumentException("Invalid Geyser skin API response", error);
                }
            } else throw new IllegalStateException("Geyser skin API returned HTTP " + statusCode);
        }
    }
}
