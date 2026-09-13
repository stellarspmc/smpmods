package spmc.smpmod.minigame.casino

import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.utils.*
import java.util.UUID

object DeuceManager: SessionManager<DeuceSession>() {
	fun startMinigame(list: List<ServerPlayer>): Boolean {
		if (list.size !in 1..4) return false
		if (checkNotCreative(list)) return false
		val session = DeuceSession(list)
		return startSession(session)
		// TODO: add betting
	}
}

class DeuceSession(players: List<ServerPlayer>): GameSession(players) {
	val cardsPlayed: MutableList<List<PokerCards>> = mutableListOf(listOf())
	val cardsInHand: Map<UUID, MutableList<PokerCards>> = mapOf()
	var turn: Int = 0; // (0 - 3)

	var uuidList: List<UUID> = listOf()

	override fun onStart() {
		if (players.size != 4) {
			TODO("add bots")
		}
		uuidList = players.map { it.uuid }
		assignCards()
	}

	override fun tick(): Boolean {
		TODO("implement mechanic...")
	}

	override fun onEnd(reason: SessionEndReason) {
		when (reason) {
			SessionEndReason.SUCCESS -> TODO("reward player")
			else -> sendError(players, reason.name)
		}
	}

	private fun assignCards() { // using uuid here because of mannequins
		val cards = PokerCards.entries // each 13 cards

		uuidList.forEach {
			//cardsInHand[it] = listOf(cards)
		}
	}
}