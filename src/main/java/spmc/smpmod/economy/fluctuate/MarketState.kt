package spmc.smpmod.economy.fluctuate

import com.mojang.math.Transformation
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Brightness
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CrossCollisionBlock
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import org.joml.Quaternionf
import org.joml.Vector3f
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.EconomyData.Companion.get
import java.util.UUID
import kotlin.collections.forEach
import kotlin.math.roundToInt

class MarketState: SavedData() {
	fun get(item: Item): FluctuationData? {
		return this.all[item]
	}

	val all: MutableMap<Item, FluctuationData> get() {
		val combined = permanentMarketMap
		temporaryMarketMap.forEach { (item, expiry) -> combined[item] = expiry.data }
		return combined
	}

	fun registerMineral(item: Item, defaultPrice: Double, fluctuation: Double) {
		val data: FluctuationData = permanentMarketMap.computeIfAbsent(item) { _ -> FluctuationData(item, defaultPrice, fluctuation) }
		data.defaultPrice = defaultPrice
		data.fluctuation = fluctuation
		setDirty()
	}

	@JvmRecord data class FluctuationExpiry(val data: FluctuationData, val expiryTick: Int)
	companion object {
		private val permanentMarketMap: MutableMap<Item, FluctuationData> = mutableMapOf()
		private val temporaryMarketMap: MutableMap<Item, FluctuationExpiry> = mutableMapOf()
		private val displayList: MutableList<UUID> = mutableListOf()
		private var rotationTick = 144000
		val CODEC: Codec<MarketState> = FluctuationData.CODEC.listOf().xmap( { datum: MutableList<FluctuationData> -> val market = MarketState()
			for (data in datum) market.registerMineral(data.mineral, data.defaultPrice, data.fluctuation)
			market }, { _ -> ArrayList(permanentMarketMap.values) })

		val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "market"), { MarketState() }, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)
		@JvmStatic val state: MarketState? get() = SMPMod.minecraftServer?.overworld()?.dataStorage?.computeIfAbsent(TYPE)

		fun buyMineral(player: ServerPlayer, item: Item, amount: Int): Double {
			val market = state ?: return -2.0
			val data = market.get(item) ?: return -2.0
			if (amount <= 0) return -2.0

			val eco = get() ?: return -2.0
			if (item == Items.DIAMOND) {
				if (!eco.changeBalance(player.getUUID(), (-amount * 100).toDouble())) return -1.0
				return (amount * 100).toDouble()
			}

			val totalCost = (data.getBulkBuyCost(amount) * 100.0).roundToInt() / 100.0
			if (!eco.changeBalance(player.getUUID(), -totalCost)) return -1.0
			data.withdraw(amount.toLong())
			market.setDirty()
			return totalCost
		}

		fun sellMineral(player: ServerPlayer, item: Item, amount: Int, multiplier: Double): Double {
			val market: MarketState = state ?: return .0
			val data = market.get(item) ?: return .0
			val eco = get()?: return .0

			if (item === Items.DIAMOND) return if (eco.changeBalance(player.getUUID(), 100 * amount * multiplier)) 100 * amount * multiplier else .0
			if (amount <= 0) return .0

			val totalPayout = (data.getBulkSellPayout(amount) * multiplier * 100.0).roundToInt() / 100.0
			if (totalPayout <= 0) return .0

			if (eco.changeBalance(player.getUUID(), totalPayout)) {
				data.deposit(amount.toLong())
				market.setDirty()
				return totalPayout
			}

			return .0
		}

		fun register() {
			val market = state?: return

			market.registerMineral(Items.HEART_OF_THE_SEA, 2000.0, 6.0)
			market.registerMineral(Items.NETHER_STAR, 1250.0, 3.0)
			market.registerMineral(Items.NETHERITE_INGOT, 750.0, 2.15)
			market.registerMineral(Items.ECHO_SHARD, 50.0, .75)
			market.registerMineral(Items.GOLD_INGOT, 10.0, .5)
			market.registerMineral(Items.EMERALD, 5.0, .25)
			market.registerMineral(Items.IRON_INGOT, 2.0, .35)
			market.registerMineral(Items.LAPIS_LAZULI, 1.0, .45)
			market.registerMineral(Items.REDSTONE, .5, .5)
			market.registerMineral(Items.COPPER_INGOT, .2, .75)
			market.registerMineral(Items.COAL, .1, 1.95)
			market.registerMineral(Items.AMETHYST_SHARD, .05, 2.15)
		}

		val chosenItems: MutableList<FluctuationData> = mutableListOf(FluctuationData(Items.ENCHANTED_GOLDEN_APPLE, 1500.0, 4.5), FluctuationData(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 750.0, 2.5), FluctuationData(Items.TOTEM_OF_UNDYING, 350.0, 1.5), FluctuationData(Items.SHULKER_SHELL, 1200.0, 4.0))

		fun addTemporaryItem(server: MinecraftServer) {
			if (chosenItems.isEmpty()) return
			val template: FluctuationData = chosenItems[server.overworld().getRandom().nextInt(chosenItems.size)]
			temporaryMarketMap[template.mineral] = FluctuationExpiry(FluctuationData(template.mineral, template.defaultPrice, template.fluctuation), server.tickCount + server.overworld().getRandom().nextInt(144000) + 144000)
		}

		fun addScreenMonitor(level: ServerLevel, pos: BlockPos): Boolean { // TODO: saved data again...
			if (displayList.isNotEmpty()) return false // this line is for
			repeat(3) { createScreen(level, pos, it) } // maybe more than 1 screen? 1 screen holds 5 data
			return true
		} // TODO: actually, why not merge the functions and put the stuff in repeat?

		private fun createScreen(level: ServerLevel, pos: BlockPos, index: Int): Boolean {
			val worldIndex = index - 2 // TODO: allow for dynamic ODD indices
			val canvas = EntityTypes.BLOCK_DISPLAY.create(level, EntitySpawnReason.TRIGGERED) ?: return false
			canvas.blockState = Blocks.STAINED_GLASS_PANE.black.defaultBlockState()
			canvas.blockState.setValue(CrossCollisionBlock.NORTH, true) // TODO: set direction by player orientation? -> e/w then n/s (inverse)
			canvas.setTransformation(Transformation(
				Vector3f(-.1f ,0f, -2f), // translation TODO: x and z
				Quaternionf(0f, 0f, 0f, 1f),
				Vector3f(1f, 3f, 4f),
				Quaternionf(0f, 0f, 0f, 1f))) // TODO: check -> quaternion needed to be changed?
			canvas.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
			canvas.brightnessOverride = Brightness(15, 15)
			level.addFreshEntity(canvas)

			repeat(5) { repeatedValue ->
				val item = EntityTypes.ITEM_DISPLAY.create(level, EntitySpawnReason.TRIGGERED)
				val text = EntityTypes.TEXT_DISPLAY.create(level, EntitySpawnReason.TRIGGERED)
				if (item == null || text == null) {
					listOfNotNull(canvas, item, text).forEach(Entity::discard)
					return false
				}

				item.itemStack = Items.HEART_OF_THE_SEA.defaultInstance // TODO: actually get top 5 items instead of
				item.setTransformation(Transformation(
					Vector3f(.3f, 2.5f - (5 - repeatedValue) * .5f, -1.4f), // translation TODO: x and z
					Quaternionf(0f, 0.70711f, 0f, 0.70711f), // TODO: translate radians (provided is 270deg)
					Vector3f(.5f, .5f, .5f),
					Quaternionf(0f, 0f, 0f, 1f)))
				item.itemTransform = ItemDisplayContext.GUI

				text.text = Component.empty() // TODO: change according to item / stats rn
				text.backgroundColor = 0
				text.setTransformation(Transformation(
					Vector3f(.3f, 2.4f - (5 - repeatedValue) * .4f, 2.5f), // translation TODO: x,y and z
					Quaternionf(0f, 0.70711f, 0f, 0.70711f), // TODO: translate radians (provided is 270deg)
					Vector3f(0.65f, 0.65f, 0.65f),
					Quaternionf(0f, 0f, 0f, 1f)))

				listOfNotNull(canvas, item, text).forEach { it.brightnessOverride = Brightness(15, 15) }
				listOfNotNull(item, text).forEach { it.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) } // TODO: use worldIndex
				listOfNotNull(canvas, item, text).forEach(level::addFreshEntity)
				listOfNotNull(item, text).forEach { displayList.add(it.uuid) } // TODO: get text (to update value)
			}

			displayList.add(canvas.uuid)
			return true
		}

		fun serverTickLoop(server: MinecraftServer) {
			val ticks = server.tickCount
			if (ticks % 900 + (server.playerList.playerCount - 1) * 125 == 0) {
				val market = state?: return
				var updated = false
				for (data in permanentMarketMap.values) if (data.applyMarketDecay(server.overworld().getRandom())) updated = true
				if (temporaryMarketMap.isNotEmpty()) temporaryMarketMap.values.forEach { data -> data.data.applyMarketDecay(server.overworld().getRandom()) }
				if (updated) market.setDirty()
			}

			if (ticks % 1200 == 0 && temporaryMarketMap.isNotEmpty()) temporaryMarketMap.values.removeIf { data -> ticks >= data.expiryTick }
			else if (ticks % rotationTick == 0) {
				addTemporaryItem(server)
				rotationTick = server.overworld().getRandom().nextInt(144000) + 144000
			}
		}

		fun processItemDeposit(player: ServerPlayer, stack: ItemStack): Double {
			val baseItem = when (stack.item.getDescriptionId()) {
				"block.minecraft.netherite_block" -> Items.NETHERITE_INGOT
				"block.minecraft.diamond_block" -> Items.DIAMOND
				"block.minecraft.gold_block" -> Items.GOLD_INGOT
				"block.minecraft.emerald_block" -> Items.EMERALD
				"block.minecraft.lapis_block" -> Items.LAPIS_LAZULI
				"block.minecraft.iron_block" -> Items.IRON_INGOT
				"block.minecraft.copper_block" -> Items.COPPER_INGOT
				"block.minecraft.redstone_block" -> Items.REDSTONE
				else -> stack.item
			}
			return sellMineral(player, baseItem, stack.count * (if (baseItem !== stack.item) 9 else 1), if (baseItem !== stack.item) .93 else 1.0)
		}
	}
}