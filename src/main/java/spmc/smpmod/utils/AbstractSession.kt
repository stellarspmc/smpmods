package spmc.smpmod.utils

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class GameSession(players: List<ServerPlayer>) {
	val id: UUID = UUID.randomUUID()
	private val mutablePlayerList: MutableList<ServerPlayer> = players.toMutableList()
	val players: List<ServerPlayer> get() = mutablePlayerList
	val isMultiplayer: Boolean get() = players.size > 1
	var endReason: SessionEndReason? = null
		private set

	open fun onStart() {}
	abstract fun tick(): Boolean /* @return true if minigame ending naturally */
	open fun onEnd(reason: SessionEndReason) {}
	open fun onPlayerLeave(player: ServerPlayer, reason: SessionEndReason): Boolean {
		mutablePlayerList.remove(player)
		return mutablePlayerList.isEmpty()
	}

	fun finish(reason: SessionEndReason) { if (this.endReason == null) this.endReason = reason }
}

abstract class SessionManager<S: GameSession> {
	private val activeSessions: MutableSet<S> = ConcurrentHashMap.newKeySet()
	private val playerToSession: MutableMap<UUID, S> = ConcurrentHashMap()

	fun register() {
		ServerTickEvents.END_SERVER_TICK.register {
			val iterator = activeSessions.iterator()
			while (iterator.hasNext()) {
				val session = iterator.next()
				val deadPlayers = session.players.filter { !it.isAlive }
				for (deadPlayer in deadPlayers) { handlePlayerDeparture(session, deadPlayer, SessionEndReason.FAIL, iterator) }
				if (session.endReason != null || !activeSessions.contains(session)) continue
				val finishedByTick = session.tick()
				val reason = session.endReason ?: if (finishedByTick) SessionEndReason.SUCCESS else null

				if (reason != null) {
					iterator.remove()
					terminateSession(session, reason)
				}
			}
		}

		ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
			val player = handler.player
			handlePlayerDeparture(playerToSession[player.uuid] ?: return@register, player, SessionEndReason.DISCONNECTED, null)
		}
	}

	protected fun startSession(session: S): Boolean {
		if (session.players.any { isInSession(it) }) return false

		activeSessions.add(session)
		for (p in session.players) { playerToSession[p.uuid] = session }

		session.onStart()
		return true
	}

	private fun handlePlayerDeparture(session: S, player: ServerPlayer, reason: SessionEndReason, iterator: MutableIterator<S>?) {
		playerToSession.remove(player.uuid)
		val shouldTerminate = session.onPlayerLeave(player, reason)

		if (shouldTerminate || session.players.isEmpty()) {
			session.finish(reason)
			if (iterator != null) iterator.remove()
			else activeSessions.remove(session)
			terminateSession(session, session.endReason ?: reason)
		}
	}

	private fun terminateSession(session: S, reason: SessionEndReason) {
		for (p in session.players) playerToSession.remove(p.uuid)
		session.onEnd(reason)
	}

	fun getSession(player: ServerPlayer): S? = playerToSession[player.uuid]
	fun isInSession(player: ServerPlayer): Boolean = playerToSession.containsKey(player.uuid)
	fun stopSession(player: ServerPlayer, reason: SessionEndReason = SessionEndReason.CANCELLED) { handlePlayerDeparture(playerToSession[player.uuid] ?: return, player, reason, null) }
}

enum class SessionEndReason {
	SUCCESS,
	FAIL,
	CANCELLED,
	DISCONNECTED
}