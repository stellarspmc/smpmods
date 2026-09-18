package spmc.smpmod.plant

import eu.pb4.polymer.core.api.block.PolymerBlock
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import spmc.smpmod.utils.BasePolymerBlockItem
import java.util.function.Supplier
import kotlin.math.pow

class SeedBlock(properties: Properties, private val cropItemSupplier: Supplier<CropItem>): CropBlock(properties.mapColor { if (it.getValue(AGE) >= 6) MapColor.COLOR_YELLOW else MapColor.PLANT }.noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.POPPED)), PolymerBlock {
	private val boneMealAffection = intArrayOf(0, 1, 1, 3, 3, 5)

	override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
		super.createBlockStateDefinition(builder)
		builder.add(BONEMEAL_COUNT)
	}

	override fun getPolymerBlockState(state: BlockState, context: PacketContext?) = Blocks.WHEAT.defaultBlockState().setValue(AGE, state.getValue(ageProperty)) // TODO

	override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState, source: BonemealSource) {
		super.performBonemeal(level, random, pos, state, source)
		val updatedState = level.getBlockState(pos)
		val currentBonemeal: Int = state.getValue(BONEMEAL_COUNT)
		if (currentBonemeal < 5 && updatedState.`is`(this)) level.setBlock(pos, updatedState.setValue(BONEMEAL_COUNT, currentBonemeal + 1), UPDATE_CLIENTS)
	}

	override fun playerDestroy(level: ServerLevel, player: ServerPlayer, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) {
		if (!level.isClientSide && isMaxAge(state)) {
			val bonemealUsed: Int = state.getValue(BONEMEAL_COUNT) // basic impl, TODO: make this more sophisticated -> impl fusing
			val finalQuality = Math.clamp((rollStarQuality(level.getRandom(), player.luck) - boneMealAffection[bonemealUsed]).toLong(), -2, 5)

			val cropItem = cropItemSupplier.get()
			val harvestedCrop = cropItem.createCropInstance(finalQuality, mutableMapOf())

			popResource(level, pos, harvestedCrop)
		}
		super.playerDestroy(level, player, pos, state, blockEntity, tool)
	}

	init {
		this.registerDefaultState(this.stateDefinition.any().setValue(ageProperty, 0).setValue(BONEMEAL_COUNT, 0))
	}

	companion object {
		val BONEMEAL_COUNT: IntegerProperty = IntegerProperty.create("bonemeal_count", 0, 5)
		private val BASE_STAR_WEIGHTS = doubleArrayOf(1000.0, 600.0, 300.0, 120.0, 35.0, 6.0)

		private fun rollStarQuality(random: RandomSource, luckBonus: Float): Int { // copied from fishing, TODO: change it bruv
			val adjustedWeights = DoubleArray(BASE_STAR_WEIGHTS.size)
			var totalWeight = 0.0

			for (star in BASE_STAR_WEIGHTS.indices) {
				val weight: Double = BASE_STAR_WEIGHTS[star] * luckBonus.toDouble().pow(star.toDouble())
				adjustedWeights[star] = weight
				totalWeight += weight
			}

			val roll = random.nextDouble() * totalWeight
			var cumulative = 0.0

			for (star in adjustedWeights.indices) {
				cumulative += adjustedWeights[star]
				if (roll < cumulative) return star
			}

			return 0
		}
	}
}

class SeedItem(block: Block, settings: Properties, vanillaItem: Item, private val seedName: String): BasePolymerBlockItem(block, settings, vanillaItem) {
	override fun buildName(stack: ItemStack) = Component.literal(seedName).withStyle(ChatFormatting.GREEN).withStyle { it.withItalic(false) }
	override fun buildLore(stack: ItemStack): MutableList<Component> = mutableListOf(Component.literal("Plant on Farmland to grow " + seedName.replace(" Seeds", "")).withStyle(ChatFormatting.GRAY).withStyle { it.withItalic(false) })
	override fun modifyItem(stack: ItemStack, stackData: ItemStack) {}
}
