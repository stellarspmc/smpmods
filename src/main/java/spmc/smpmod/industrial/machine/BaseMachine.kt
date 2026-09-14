package spmc.smpmod.industrial.machine

import eu.pb4.polymer.core.api.block.PolymerBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.Containers
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeManager.CachedCheck
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.function.Supplier
import kotlin.math.min

abstract class BaseMachineBlock<E: BaseMachineEntity<*, *>> protected constructor(properties: Properties, private val blockEntityTypeSupplier: Supplier<BlockEntityType<E>>, private val blockEntityFactory: BlockEntityType.BlockEntitySupplier<E>): BaseEntityBlock(properties.requiresCorrectToolForDrops().strength(3.5f).mapColor(MapColor.STONE)), PolymerBlock {
	override fun <T: BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
		if (level is ServerLevel) return createTickerHelper(type, this.blockEntityTypeSupplier.get()) { _, _, _, entity -> entity.serverTick(level) }
		return null
	}

	override fun newBlockEntity(pos: BlockPos, state: BlockState) = this.blockEntityFactory.create(pos, state)
	override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL
	override fun affectNeighborsAfterRemoval(state: BlockState, level: ServerLevel, pos: BlockPos, movedByPiston: Boolean) { Containers.updateNeighboursAfterDestroy(state, level, pos) }
}

abstract class BaseMachineEntity<I: RecipeInput, R: Recipe<I>> protected constructor(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, containerSize: Int, recipeType: RecipeType<R>, @JvmField protected val input: IntArray, protected val output: IntArray): BlockEntity(type, pos, state), WorldlyContainer {
	protected var items: NonNullList<ItemStack>
	var progressTicks: Int = 0
		protected set
	var maxProgressTicks: Int = 500
		protected set
	private val quickCheck: CachedCheck<I, R>

	init {
		this.items = NonNullList.withSize(containerSize, ItemStack.EMPTY)
		this.quickCheck = RecipeManager.createCheck(recipeType)
	}

	override fun loadAdditional(input: ValueInput) {
		super.loadAdditional(input)
		this.items = NonNullList.withSize(this.containerSize, ItemStack.EMPTY)
		ContainerHelper.loadAllItems(input, this.items)
		this.progressTicks = input.getShortOr("progress_ticks", 0.toShort())
		this.maxProgressTicks = input.getShortOr("max_progress_ticks", 500.toShort())
	}

	override fun saveAdditional(output: ValueOutput) {
		super.saveAdditional(output)
		ContainerHelper.saveAllItems(output, this.items)
		output.putShort("progress_ticks", this.progressTicks.toShort())
		output.putShort("max_progress_ticks", this.maxProgressTicks.toShort())
	}

	override fun isEmpty(): Boolean {
		for (stack in this.items) if (!stack.isEmpty) return false
		return true
	}

	override fun setItem(slot: Int, stack: ItemStack) {
		this.items[slot] = stack
		stack.limitSize(this.getMaxStackSize(stack))
		this.setChanged()
	}

	fun serverTick(level: ServerLevel) {
		var changed = false
		val input = createRecipeInput()

		if (input != null && !input.isEmpty) {
			if (quickCheck.getRecipeFor(input, level).isPresent) {
				val recipeHolder = quickCheck.getRecipeFor(input, level).get()
				val recipe = recipeHolder.value()
				val resultStack = getRecipeResult(recipe, level)

				if (canOutput(resultStack)) {
					this.maxProgressTicks = getRecipeProcessTime(recipe)
					this.progressTicks++
					onProcessTick(level, this.progressTicks, this.maxProgressTicks)

					if (this.progressTicks >= this.maxProgressTicks) {
						this.progressTicks = 0

						consumeIngredients(recipe, input)
						produceOutput(resultStack)
						onProcessComplete(level, resultStack)
						changed = true
					}
				} else resetProgress()
			} else resetProgress()
		} else if (this.progressTicks > 0) {
			resetProgress()
			changed = true
		}

		if (changed) setChanged(level, this.blockPos, this.blockState)
	}

	protected fun canOutput(recipeResult: ItemStack): Boolean {
		if (recipeResult.isEmpty) return true
		for (slot in output) {
			val current = getItem(slot)
			if (current.isEmpty) return true
			if (ItemStack.isSameItemSameComponents(current, recipeResult) && current.count + recipeResult.count <= min(maxStackSize, recipeResult.maxStackSize)) return true
		}
		return false
	}

	protected fun produceOutput(recipeResult: ItemStack) {
		if (recipeResult.isEmpty) return
		for (slot in output) {
			val current = getItem(slot)
			if (!current.isEmpty && ItemStack.isSameItemSameComponents(current, recipeResult)) {
				val maxCount = min(maxStackSize, recipeResult.maxStackSize)
				if (current.count + recipeResult.count <= maxCount) {
					current.grow(recipeResult.count)
					return
				}
			}
		}

		for (slot in output) {
			if (getItem(slot).isEmpty) {
				setItem(slot, recipeResult.copy())
				return
			}
		}
	}

	protected abstract fun onProcessTick(level: ServerLevel, currentProgress: Int, maxProgress: Int)
	protected abstract fun onProcessComplete(level: ServerLevel, result: ItemStack)
	protected abstract fun createRecipeInput(): I?
	protected abstract fun getRecipeResult(recipe: R, level: ServerLevel): ItemStack
	protected abstract fun getRecipeProcessTime(recipe: R): Int
	protected abstract fun consumeIngredients(recipe: R, input: I)

	override fun getItem(slot: Int) = this.items[slot]
	override fun removeItem(slot: Int, amount: Int) = ContainerHelper.removeItem(this.items, slot, amount)
	override fun removeItemNoUpdate(slot: Int) = ContainerHelper.takeItem(this.items, slot)
	override fun stillValid(player: Player) = Container.stillValidBlockEntity(this, player)
	override fun getSlotsForFace(direction: Direction) = if (direction == Direction.DOWN) output else input // TODO
	override fun canPlaceItemThroughFace(slot: Int, itemStack: ItemStack, direction: Direction?) = input.any { it == slot }
	override fun canTakeItemThroughFace(slot: Int, itemStack: ItemStack, direction: Direction) = output.any { it == slot }
	override fun clearContent() { this.items.clear() }
	override fun getContainerSize() = this.items.size
	private fun resetProgress() { if (this.progressTicks > 0) this.progressTicks = 0 }
}