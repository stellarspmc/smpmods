package spmc.smpmod.utils.sessions

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

abstract class MechanicManager {
	private val sessions: MutableMap<UUID, MechanicSession> = mutableMapOf()
	fun register() { ServerTickEvents.END_SERVER_TICK.register { sessions.entries.removeIf { entry -> entry.value.tick() } } }

	abstract fun startMinigame(player: ServerPlayer)
	fun isInSession(playerUuid: UUID) = sessions.containsKey(playerUuid)
	fun cancelSession(playerUuid: UUID) = sessions.remove(playerUuid)
}