package spmc.smpmod.fishing.mechanic

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FishingHook
import spmc.smpmod.fishing.RodItem
import spmc.smpmod.fishing.RodTiers
import java.util.*

object FishingManager {
	private val ACTIVE_SESSIONS: MutableMap<UUID, FishingSession> = mutableMapOf()
	fun register() { ServerTickEvents.END_SERVER_TICK.register { ACTIVE_SESSIONS.entries.removeIf { entry -> entry.value.tick() } } }

	@JvmStatic
	fun startMinigame(player: ServerPlayer, hook: FishingHook) {
		if (player.level().dimension().identifier().namespace != "minecraft") return
		if (ACTIVE_SESSIONS.containsKey(player.getUUID())) return
		ACTIVE_SESSIONS[player.getUUID()] = FishingSession(player, hook, (player.mainHandItem.item as? RodItem)?.tier ?: RodTiers.NORMAL)
	}

	fun isFishing(playerUuid: UUID) = ACTIVE_SESSIONS.containsKey(playerUuid)
	fun cancelMinigame(playerUuid: UUID) = ACTIVE_SESSIONS.remove(playerUuid)
}