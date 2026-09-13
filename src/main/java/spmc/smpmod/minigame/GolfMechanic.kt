package spmc.smpmod.minigame

import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.utils.*

object GolfMechanic: SessionManager<GolfSession>() {
	fun startMinigame(player: ServerPlayer): Boolean {
		if (checkNotCreative(player)) return false
		val session = GolfSession(player)
		return startSession(session)
	}
}

class GolfSession(val player: ServerPlayer): GameSession(listOf(player)) {
	override fun tick(): Boolean {
		TODO("implement golf mechanic...")
	}

	override fun onEnd(reason: SessionEndReason) {
		when (reason) {
			SessionEndReason.SUCCESS -> TODO("reward player")
			else -> sendError(player, message = reason.name)
		}
	}
}