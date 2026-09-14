package spmc.smpmod.minigame.casino

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import spmc.smpmod.utils.*
import java.util.*

object DeuceManager: SessionManager<DeuceSession>() {
	fun startMinigame(list: List<ServerPlayer>): Boolean {
		if (list.size !in 1..4) return false
		if (checkNotCreative(list)) return false
		val session = DeuceSession(list)
		return startSession(session)
		// TODO: add betting
	}
}

class DeuceSession(players: List<ServerPlayer>) : GameSession(players) {
	val seats: MutableList<UUID> = mutableListOf()
	val cardsInHand: MutableMap<UUID, MutableList<PokerCard>> = mutableMapOf()
	val selectedIndices: MutableMap<UUID, MutableSet<Int>> = mutableMapOf()
	val focusedIndex: MutableMap<UUID, Int> = mutableMapOf()

	var currentTurnIndex = 0
	var lastPlayedHand: List<PokerCard> = emptyList()
	var passCount = 0
	private var tickCounter = 0

	private val handMapBaseId = 1000 // TODO: map testing

	override fun onStart() {
		seats.clear()
		players.forEach { seats.add(it.uuid) }

		dealCards()
		players.forEach {
			selectedIndices[it.uuid] = mutableSetOf()
			focusedIndex[it.uuid] = 0
			setupPlayerHotbar(it)
		}
	}

	override fun tick(): Boolean {
		tickCounter++

		val currentPlayerUUID = seats[currentTurnIndex]
		val activePlayer = players.find { it.uuid == currentPlayerUUID }

		players.forEach {
			val isTurn = it.uuid == currentPlayerUUID
			val statusMessage = if (isTurn) "§a§lYOUR TURN! §fSelected: ${selectedIndices[it.uuid]?.size ?: 0} cards"
			else "§7Waiting for §e${activePlayer?.scoreboardName ?: "Player"}§7..." // todo: use components
			it.sendSystemMessage(Component.literal(statusMessage), true)
		}

		if (tickCounter % 5 == 0) renderPlayerHands()
		return false
	}

	private fun dealCards() {
		val deck = PokerCard.FULL_DECK.shuffled().toMutableList()
		players.forEachIndexed { i, player ->
			val hand = deck.subList(i * 13, (i + 1) * 13).sorted().toMutableList()
			cardsInHand[player.uuid] = hand
		}
	}

	private fun renderPlayerHands() {
		players.forEach {
			val hand = cardsInHand[it.uuid] ?: return@forEach
			val selected = selectedIndices[it.uuid] ?: emptySet()
			val focused = focusedIndex[it.uuid] ?: 0

			hand.forEachIndexed { i, card ->
				if (i < 13) {
					val mapId = handMapBaseId + i
					val isSelected = selected.contains(i) || (i == focused)

					//CardMapRenderer.sendCardMap(player, mapId, card, isSelected)
				}
			}
		}
	}

	fun handleHotbarClick(player: ServerPlayer, slot: Int) {
		val uuid = player.uuid
		if (seats[currentTurnIndex] != uuid) return

		val hand = cardsInHand[uuid] ?: return
		val currentFocus = focusedIndex[uuid] ?: 0

		when (slot) {
			0 -> focusedIndex[uuid] = (currentFocus - 1 + hand.size) % hand.size
			2 -> focusedIndex[uuid] = (currentFocus + 1) % hand.size
			1 -> {
				val selected = selectedIndices.getOrPut(uuid) { mutableSetOf() }
				if (selected.contains(currentFocus)) selected.remove(currentFocus)
				else selected.add(currentFocus)
			}
			7 -> passTurn(player)
			8 -> submitSelectedPlay(player)
		}
	}

	private fun submitSelectedPlay(player: ServerPlayer) {
		val uuid = player.uuid
		val hand = cardsInHand[uuid] ?: return
		val selected = selectedIndices[uuid] ?: return

		val cardsToPlay = selected.map { hand[it] }
		if (isValidPlay(cardsToPlay, lastPlayedHand)) {
			hand.removeAll(cardsToPlay)
			lastPlayedHand = cardsToPlay
			selected.clear()
			focusedIndex[uuid] = 0

			if (hand.isEmpty()) {
				finish(SessionEndReason.SUCCESS)
				return
			}
			advanceTurn()
		} else sendError(player, message = "Invalid card combination!")
	}

	private fun passTurn(player: ServerPlayer) {
		if (lastPlayedHand.isNotEmpty()) {
			passCount++
			selectedIndices[player.uuid]?.clear()
			advanceTurn()
		}
	}

	private fun advanceTurn() {
		currentTurnIndex = (currentTurnIndex + 1) % players.size
		if (passCount >= players.size - 1) {
			lastPlayedHand = emptyList()
			passCount = 0
		}
	}

	override fun onEnd(reason: SessionEndReason) {
		players.forEach {
			it.inventory.clearContent()
			it.sendSystemMessage(Component.literal("§eGame Ended: ${reason.name}"))
		}
	}
}

private fun setupPlayerHotbar(player: ServerPlayer) {
	player.inventory.clearContent()

	// Hotbar control items
	player.inventory.setItem(0, createNamedItem(Items.DYE.lightBlue, "§b◀ Prev Card"))
	player.inventory.setItem(1, createNamedItem(Items.DYE.lime, "§aToggle Select"))
	player.inventory.setItem(2, createNamedItem(Items.DYE.lightBlue, "§bNext Card ▶"))

	player.inventory.setItem(5, createNamedItem(Items.HOPPER, "§eSort: Rank / Suit"))
	player.inventory.setItem(7, createNamedItem(Items.CONCRETE.red, "§c[PASS]"))
	player.inventory.setItem(8, createNamedItem(Items.CONCRETE.green, "§a[PLAY HAND]"))

	player.inventoryMenu.broadcastChanges()
}

private fun createNamedItem(item: Item, name: String): ItemStack {
	val stack = ItemStack(item)
	stack.set(DataComponents.CUSTOM_NAME, Component.literal(name))
	return stack
}

fun isValidPlay(cards: List<PokerCard>, currentTable: List<PokerCard>): Boolean {
	if (cards.isEmpty()) return false
	val sorted = cards.sorted()

	if (currentTable.isEmpty()) return isSingle(sorted) || isPair(sorted) || isTriple(sorted) || isFiveCardHand(sorted)
	if (cards.size != currentTable.size) return false
	return when (cards.size) {
		1 -> sorted.last() > currentTable.last()
		2 -> isPair(sorted) && sorted.last() > currentTable.last()
		3 -> isTriple(sorted) && sorted.last() > currentTable.last()
		5 -> validateFiveCardHand(sorted, currentTable.sorted())
		else -> false
	}
}

private fun isSingle(cards: List<PokerCard>) = cards.size == 1
private fun isPair(cards: List<PokerCard>) = cards.size == 2 && cards[0].rank == cards[1].rank
private fun isTriple(cards: List<PokerCard>) = cards.size == 3 && cards.all { it.rank == cards[0].rank }
private fun isFiveCardHand(cards: List<PokerCard>) = cards.size == 5 && (isStraight(cards) || isFlush(cards) || isFullHouse(cards) || isFourOfAKind(cards))
private fun isStraight(cards: List<PokerCard>): Boolean {
	for (i in 0..3) if (cards[i + 1].rank.weight != cards[i].rank.weight + 1) return false
	return true
}

private fun isFlush(cards: List<PokerCard>) = cards.all { it.suit == cards[0].suit }
private fun isFullHouse(cards: List<PokerCard>): Boolean {
	val groups = cards.groupBy { it.rank.weight }
	return groups.size == 2 && (groups.values.any { it.size == 3 })
}

private fun isFourOfAKind(cards: List<PokerCard>): Boolean {
	val groups = cards.groupBy { it.rank.weight }
	return groups.size == 2 && (groups.values.any { it.size == 4 })
}

private fun validateFiveCardHand(newHand: List<PokerCard>, tableHand: List<PokerCard>): Boolean {
	val newRank = getFiveCardCategoryRank(newHand)
	val tableRank = getFiveCardCategoryRank(tableHand)

	if (newRank > tableRank) return true
	if (newRank < tableRank) return false
	return newHand.last() > tableHand.last()
}

private fun getFiveCardCategoryRank(hand: List<PokerCard>): Int {
	val isS = isStraight(hand)
	val isF = isFlush(hand)
	return when {
		isS && isF -> 5
		isFourOfAKind(hand) -> 4
		isFullHouse(hand) -> 3
		isF -> 2
		isS -> 1
		else -> 0
	}
}