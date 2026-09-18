package spmc.smpmod.industrial.machine

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import spmc.smpmod.industrial.CompressorRecipe
import spmc.smpmod.registry.IndustrialRegistry
import kotlin.math.max

class SculkCompressorBlock(properties: Properties): BaseMachineBlock<SculkCompressorEntity>(properties, { IndustrialRegistry.SCULK_ENTITY!! }, ::SculkCompressorEntity) {
	//override fun codec(): simpleCodec = CODEC
	override fun getPolymerBlockState(state: BlockState, context: PacketContext?) = Blocks.SCULK_CATALYST.defaultBlockState().setValue(BlockStateProperties.BLOOM, true)
	override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
		if (!level.isClientSide && player is ServerPlayer && level.getBlockEntity(pos) is SculkCompressorEntity) CompressorUI(player, level.getBlockEntity(pos) as SculkCompressorEntity).open()
		return InteractionResult.SUCCESS
	}

	internal class CompressorUI(player: ServerPlayer, private val blockEntity: SculkCompressorEntity): SimpleGui(MenuType.GENERIC_9x3, player, false) {
		init {
			this.setTitle(Component.literal("Sculk Compressor").withColor(TextColor.fromRgb(0x00AAAA)))
			for (i in 0 .. 26) this.setSlot(i, GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")))
			this.setSlot(INPUT_SLOT, Slot(blockEntity, 0, 0, 0))
			this.setSlot(OUTPUT_SLOT, object: Slot(blockEntity, 1, 0, 0) { override fun mayPlace(stack: ItemStack) = false })
			setLockPlayerInventory(false)
		}

		override fun onAnyClick(index: Int, type: ClickType, action: ContainerInput) = !(index == OUTPUT_SLOT && (action == ContainerInput.PICKUP_ALL || action == ContainerInput.PICKUP || action == ContainerInput.SWAP)) && super.onAnyClick(index, type, action)
		override fun onTick() {
			val progress: Int = blockEntity.progressTicks
			val percent = ((progress.toFloat() / 1.coerceAtLeast(blockEntity.maxProgressTicks)) * 100).toInt()

			if (progress > 0) {
				val filledBars = percent / 10
				val progressBar = Component.empty().append(Component.literal("█".repeat(filledBars)).withStyle(ChatFormatting.AQUA)).append(Component.literal("▒".repeat(10 - filledBars)).withStyle(ChatFormatting.GRAY))
				val progressText = Component.literal("Progress: ").withStyle(ChatFormatting.GRAY).append(Component.literal("$percent%").withStyle(ChatFormatting.YELLOW))

				this.setSlot(
					PROCESS_SLOT, GuiElementBuilder(Items.ECHO_SHARD).setName(
						Component.literal("Compressing...").withColor(TextColor.fromRgb(0x00AAAA)).withStyle(ChatFormatting.BOLD)
					).addLoreLine(progressBar).addLoreLine(progressText).setCount(max(1, (percent * 64) / 100)).glow()
				)
			} else this.setSlot(PROCESS_SLOT, GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("Waiting for Input...").withStyle(ChatFormatting.GRAY)))
		}

		companion object {
			const val INPUT_SLOT = 10
			const val PROCESS_SLOT = 13
			const val OUTPUT_SLOT = 16
		}
	}

	//companion object {
		//val CODEC = simpleCodec(::SculkCompressorBlock)
	//}
}

class SculkCompressorEntity(pos: BlockPos, state: BlockState): BaseMachineEntity<SingleRecipeInput, CompressorRecipe>(IndustrialRegistry.SCULK_ENTITY!!, pos, state, 2, IndustrialRegistry.COMPRESSOR_TYPE!!, intArrayOf(0), intArrayOf(1)) {
	override fun onProcessTick(level: ServerLevel, currentProgress: Int, maxProgress: Int) {
		if (currentProgress % 6 == 0) {
			val pitch = 0.6f + ((currentProgress.toFloat() / maxProgress) * .8f)
			level.playSound(null, this.worldPosition, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, .4f, pitch)
		}
	}

	override fun onProcessComplete(level: ServerLevel, result: ItemStack) {
		level.playSound(null, this.worldPosition, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, .8f, 1.2f)
		level.playSound(null, this.worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, .3f, .5f)
		level.sendParticles(ParticleTypes.SCULK_SOUL, this.worldPosition.x + .5, this.worldPosition.y + 1.1, this.worldPosition.z + .5, 15, .25, .25, .25, .03)
	}

	override fun createRecipeInput(): SingleRecipeInput? {
		val stack = getItem(input[0])
		return if (stack.isEmpty) null else SingleRecipeInput(stack)
	}

	override fun getRecipeResult(recipe: CompressorRecipe, level: ServerLevel) = recipe.result.create()
	override fun getRecipeProcessTime(recipe: CompressorRecipe) = recipe.processTime
	override fun consumeIngredients(recipe: CompressorRecipe, input: SingleRecipeInput) { getItem(this@SculkCompressorEntity.input[0]).shrink(recipe.count) }
}