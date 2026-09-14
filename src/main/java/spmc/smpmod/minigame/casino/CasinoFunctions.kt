package spmc.smpmod.minigame.casino

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.saveddata.maps.MapId
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

enum class CardSuit(val weight: Int, val symbol: String, val color: TextColor) {
	DIAMONDS(1, "♦", TextColor.RED),
	CLUBS(2, "♣", TextColor.fromRgb(1118505)),
	HEARTS(3, "♥", TextColor.RED),
	SPADES(4, "♠", TextColor.fromRgb(1118505));

	companion object {
		val DEFAULT_ORDER = listOf(DIAMONDS, CLUBS, HEARTS, SPADES)
	}
}

enum class CardRank(val weight: Int, val symbol: String) {
	THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"), SIX(6, "6"), SEVEN(7, "7"),
	EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"), JACK(11, "J"), QUEEN(12, "Q"),
	KING(13, "K"), ACE(14, "A"), TWO(15, "2")
}

data class PokerCard(val rank: CardRank, val suit: CardSuit) : Comparable<PokerCard> {
	val absoluteValue: Int get() = (rank.weight * 10) + suit.weight
	override fun compareTo(other: PokerCard) = this.absoluteValue.compareTo(other.absoluteValue)
	fun getDisplayName() = Component.literal("${suit.symbol} ${rank.symbol}").withColor(suit.color)

	companion object {
		val FULL_DECK: List<PokerCard> by lazy { CardRank.entries.flatMap { r -> CardSuit.entries.map { PokerCard(r, it) } } }

	}
}

object CasinoBot {
	fun spawnBot(): Mannequin? {
		return null
	}
}

class CardMapRenderer { // planned for deuces only
	private val personalDisplayEntityIds: MutableMap<UUID, List<Int>> = mutableMapOf()
	private val tableDisplayEntityIds: MutableList<Int> = mutableListOf()
	fun updatePersonalHand(player: ServerPlayer, hand: List<PokerCard>, selectedIndices: Set<Int>, focusedIndex: Int) {
		var entityIds = personalDisplayEntityIds[player.uuid]

		if (entityIds == null || entityIds.size != 13) {
			clearPersonalHand(player)
			entityIds = List(13) { UUID.randomUUID().hashCode() }
			personalDisplayEntityIds[player.uuid] = entityIds

			entityIds.forEach {
				val spawnPacket = ClientboundAddEntityPacket(it, UUID.randomUUID(), player.x, player.y, player.z, 0f, 0f, EntityTypes.BLOCK_DISPLAY, 0, Vec3.ZERO, 0.0)
				player.connection.send(spawnPacket)
			}
		}

		val transforms = calculateHandTransforms(hand.size, selectedIndices, focusedIndex)
		hand.forEachIndexed { index, card ->
			if (index < 13) {
				val entityId = entityIds[index]
				val itemStack = createMapItemStack(card)
				val transform = transforms[index]

				player.connection.send(createItemDisplayDataPacket(entityId, itemStack, transform))
			}
		}
	}

	fun clearPersonalHand(player: ServerPlayer) { player.connection.send(ClientboundRemoveEntitiesPacket(*(personalDisplayEntityIds.remove(player.uuid) ?: return).toIntArray())) }
	fun updateTableCards(tablePlayers: List<ServerPlayer>, cardsOnTable: List<PokerCard>, tableCenter: Vec3) {
		if (tableDisplayEntityIds.size != cardsOnTable.size) {
			clearTableCards(tablePlayers)
			tableDisplayEntityIds.addAll(List(cardsOnTable.size) { UUID.randomUUID().hashCode() })
		}

		cardsOnTable.forEachIndexed { index, card ->
			val entityId = tableDisplayEntityIds[index]
			val itemStack = createMapItemStack(card)
			val offset = Vec3((index - cardsOnTable.size / 2.0) * .3, .85, .0)
			val cardPos = tableCenter.add(offset)

			tablePlayers.forEach {
				it.connection.send(ClientboundAddEntityPacket(entityId, UUID.randomUUID(), cardPos.x, cardPos.y, cardPos.z, 0f, 0f, EntityTypes.BLOCK_DISPLAY, 0, Vec3.ZERO, .0))
				it.connection.send(createItemDisplayDataPacket(entityId, itemStack, CardTransform(Vector3f(0f, 0f, 0f), Quaternionf().rotateX(Math.toRadians(90.0).toFloat()), Vector3f(.35f, .35f, .35f))))
			}
		}
	}

	fun clearTableCards(tablePlayers: List<ServerPlayer>) {
		if (tableDisplayEntityIds.isEmpty()) return
		val removePacket = ClientboundRemoveEntitiesPacket(*tableDisplayEntityIds.toIntArray())
		tablePlayers.forEach { it.connection.send(removePacket) }
		tableDisplayEntityIds.clear()
	}

	private fun createItemDisplayDataPacket(entityId: Int, itemStack: ItemStack, transform: CardTransform): ClientboundSetEntityDataPacket {
		val syncedData = mutableListOf<SynchedEntityData.DataValue<*>>()
		// 23: Displayed Item
		// 11: Translation Vector3f
		// 12: Scale Vector3f
		// 13: Left Rotation Quaternionf

		return ClientboundSetEntityDataPacket(entityId, syncedData)
	}
}

data class CardTransform(val positionOffset: Vector3f, val rotation: Quaternionf, val scale: Vector3f)
fun calculateHandTransforms(cardCount: Int, selectedIndices: Set<Int>, focusedIndex: Int): List<CardTransform> {
	val transforms = mutableListOf<CardTransform>()
	if (cardCount == 0) return transforms

	val maxArcAngle = Math.toRadians(36.0)
	val angleStep = if (cardCount > 1) maxArcAngle / (cardCount - 1) else 0.0
	val startAngle = -maxArcAngle / 2.0

	val radius = .85f
	for (i in 0 until cardCount) {
		val angle = startAngle + (i * angleStep)
		val x = (radius * sin(angle)).toFloat()
		val z = (radius * cos(angle)).toFloat() - 0.2f
		val isSelected = selectedIndices.contains(i)
		val isFocused = (i == focusedIndex)
		val y = when {
			isSelected -> -.25f
			isFocused -> -.35f
			else -> -.45f
		}

		val rotation = Quaternionf().rotateY(angle.toFloat()).rotateX(Math.toRadians(55.0).toFloat()) // todo
		val scale = if (isSelected || isFocused) Vector3f(.28f, .28f, .28f) else Vector3f(.23f, .23f, .23f)

		transforms.add(CardTransform(Vector3f(x, y, z), rotation, scale))
	}

	return transforms
}

private val CARD_MAP_IDS: Map<PokerCard, Int> = mapOf(
	// Spades
	PokerCard(CardRank.TWO, CardSuit.SPADES) to 101,
	PokerCard(CardRank.ACE, CardSuit.SPADES) to 102,
	// ...
	PokerCard(CardRank.THREE, CardSuit.DIAMONDS) to 152
)

fun createMapItemStack(card: PokerCard): ItemStack {
	val mapIdInt = CARD_MAP_IDS[card] ?: 0
	val stack = ItemStack(Items.FILLED_MAP)
	stack.set(DataComponents.MAP_ID, MapId(mapIdInt))
	return stack
}