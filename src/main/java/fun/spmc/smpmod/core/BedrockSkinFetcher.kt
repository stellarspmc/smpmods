package `fun`.spmc.smpmod.core

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.google.gson.JsonParser
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import `fun`.spmc.smpmod.SMPMod
import `fun`.spmc.smpmod.mixin.AccessorGameProfile
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ClientboundRespawnPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ChunkMap
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.PositionMoveRotation
import org.geysermc.floodgate.api.FloodgateApi
import java.io.StringReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function

object BedrockSkinFetcher {
    private val SKIN_REQUEST_TIMEOUT: Duration? = Duration.ofSeconds(8L)
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun restoreSkin(server: MinecraftServer, player: ServerPlayer) {
        val uuid = player.getUUID()
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return
        val floodgatePlayer = FloodgateApi.getInstance().getPlayer(uuid) ?: return

        val xuid = floodgatePlayer.xuid
        fetchAndApplySkin(server, uuid, player.gameProfile.name(), xuid, 0)
    }

    private fun fetchAndApplySkin(server: MinecraftServer, playerId: UUID, playerName: String, xuid: String, attempt: Int) {
        GeyserSkinClient.fetchSkin(xuid).thenAccept(Consumer { skinOpt: Optional<SkinProperty> ->
            if (skinOpt.isEmpty) scheduleRetry(server, playerId, playerName, xuid, attempt)
            else server.execute { applySkin(server, playerId, skinOpt.get()) }
        }).exceptionally(Function { _: Throwable? ->
            SMPMod.modLogger.warn("Failed to fetch Bedrock skin for {} ({}), retrying...", playerName, xuid)
            scheduleRetry(server, playerId, playerName, xuid, attempt)
            null
        })
    }

    private fun scheduleRetry(server: MinecraftServer, playerId: UUID, playerName: String, xuid: String, attempt: Int) {
        if (attempt >= 5) {
            SMPMod.modLogger.info("No converted Bedrock skin available for {} after max attempts.", playerName)
            return
        }

        scheduler.schedule({
            server.execute {
                val player = server.playerList.getPlayer(playerId)?: return@execute
                if (player.connection.isAcceptingMessages) fetchAndApplySkin(server, playerId, playerName, xuid, attempt + 1)
            }
        }, 850L, TimeUnit.MILLISECONDS)
    }

    private fun applySkin(server: MinecraftServer, playerId: UUID, skin: SkinProperty) {
        val player = server.playerList.getPlayer(playerId) ?: return

        val profile = player.gameProfile
        val currentProperties = profile.properties()
        val map: Multimap<String, Property> = LinkedHashMultimap.create()
        for ((key, value) in currentProperties.entries()) if (key != "textures") map.put(key, value)

        map.put("textures", Property("textures", skin.value, skin.signature))
        val newProperties = PropertyMap(map)
        (profile as AccessorGameProfile).setProperties(newProperties) // shush
        val removePacket = ClientboundPlayerInfoRemovePacket(listOf(player.getUUID()))
        val addPacket = ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), listOf(player))

        for (other in server.playerList.players) {
            other.connection.send(removePacket)
            other.connection.send(addPacket)
        }

        val level = player.level()
        val chunkMap: ChunkMap = level.chunkSource.chunkMap
        chunkMap.removeEntity(player)
        chunkMap.addEntity(player)

        player.connection.send(ClientboundRespawnPacket(player.createCommonSpawnInfo(level), ClientboundRespawnPacket.KEEP_ALL_DATA))
        player.connection.send(ClientboundPlayerPositionPacket(0, PositionMoveRotation.of(player), mutableSetOf()))
        server.playerList.sendAllPlayerInfo(player)
        player.onUpdateAbilities()
        player.inventoryMenu.sendAllDataToRemote()

        SMPMod.modLogger.info("Successfully restored Bedrock skin for {}", player.scoreboardName)
    }


    @JvmRecord
    private data class SkinProperty(val value: String?, val signature: String?)

    private object GeyserSkinClient {
        private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(SKIN_REQUEST_TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build()

        fun fetchSkin(xuid: String): CompletableFuture<Optional<SkinProperty>> {
            if (xuid.isBlank()) return CompletableFuture.completedFuture(Optional.empty<SkinProperty>())
            val request = HttpRequest.newBuilder(URI.create("https://api.geysermc.org/v2/skin/$xuid")).timeout(SKIN_REQUEST_TIMEOUT).header("Accept", "application/json").GET().build()
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply { res: HttpResponse<String> -> parseSkinResponse(res) }
        }

        fun parseSkinResponse(response: HttpResponse<String>): Optional<SkinProperty> {
            val statusCode = response.statusCode()
            if (statusCode == 404) return Optional.empty<SkinProperty?>()

            if (statusCode in 200..<300) {
                try {
                    StringReader(response.body()).use { reader ->
                        val json = JsonParser.parseReader(reader).getAsJsonObject()
                        if (!json.has("value") || json.get("value").isJsonNull) return Optional.empty<SkinProperty?>()

                        val value = json.get("value").asString
                        val signature = if (json.has("signature") && !json.get("signature").isJsonNull) json.get("signature").asString else null

                        if (value.isBlank()) return Optional.empty<SkinProperty?>()
                        return Optional.of<SkinProperty?>(SkinProperty(value, signature))
                    }
                } catch (error: Exception) {
                    throw IllegalArgumentException("Invalid Geyser skin API response", error)
                }
            } else throw IllegalStateException("Geyser skin API returned HTTP $statusCode")
        }
    }
}