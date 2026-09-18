package spmc.smpmod.minigame.casino

import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.utils.GameSession
import spmc.smpmod.utils.SessionManager
import spmc.smpmod.utils.anyInCreative
import java.util.*

object BlackjackManager: SessionManager<BlackjackSession>() {
	fun startMinigame(list: List<ServerPlayer>): Boolean {
		if (list.size !in 1..4) return false
		if (anyInCreative(list)) return false
		val session = BlackjackSession(list)
		return startSession(session)
		// TODO: add betting
	}
}

class BlackjackSession(players: List<ServerPlayer>) : GameSession(players) {
	val seats: MutableList<UUID> = mutableListOf()
	val cardsInHand: MutableMap<UUID, MutableList<PokerCard>> = mutableMapOf()
	var dealerCards: MutableList<PokerCard> = mutableListOf()

	override fun onStart() {
		seats.clear()
		players.forEach { seats.add(it.uuid) }

		val deck = PokerCard.FULL_DECK.shuffled().toMutableList()
		players.forEach { cardsInHand[it.uuid] = deck.take(2).toMutableList() }
		// add hotbar items
	}

	override fun tick(): Boolean {
		return false
	}
}