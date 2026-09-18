package spmc.smpmod.industrial.machine

import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import spmc.smpmod.industrial.SmelterRecipe
import spmc.smpmod.industrial.TripleRecipeInput
import spmc.smpmod.registry.IndustrialRegistry

class SmelteryBlock(properties: Properties): BaseMachineBlock<SmelteryEntity>(properties, { IndustrialRegistry.SMELTERY_ENTITY!! }, ::SmelteryEntity) {
	//public override fun codec() = CODEC
	override fun getPolymerBlockState(state: BlockState, context: PacketContext?) = Blocks.SMOKER.defaultBlockState().setValue(BlockStateProperties.LIT, true)

	override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
		if (!level.isClientSide && player is ServerPlayer) player.sendSystemMessage(Component.literal("The smeltery is a work in progress...")) //new SmeltryUI(serverPlayer).open();
		return InteractionResult.SUCCESS
	}

	//companion object {
	//	val CODEC = simpleCodec(::SmelteryBlock)
	//}
}

class SmelteryEntity(pos: BlockPos, state: BlockState): BaseMachineEntity<TripleRecipeInput, SmelterRecipe>(IndustrialRegistry.SMELTERY_ENTITY!!, pos, state, 4, IndustrialRegistry.SMELTERY_TYPE!!, intArrayOf(0, 1, 2), intArrayOf(3)) {
	override fun onProcessTick(level: ServerLevel, currentProgress: Int, maxProgress: Int) {}
	override fun onProcessComplete(level: ServerLevel, result: ItemStack) {}

	override fun createRecipeInput(): TripleRecipeInput? {
		val in1 = getItem(0)
		val in2 = getItem(1)
		val in3 = getItem(2)

		if (in1.isEmpty && in2.isEmpty && in3.isEmpty) return null
		return TripleRecipeInput(in1, in2, in3)
	}

	override fun getRecipeResult(recipe: SmelterRecipe, level: ServerLevel) = recipe.result.create()
	override fun getRecipeProcessTime(recipe: SmelterRecipe) = recipe.processTime
	override fun consumeIngredients(recipe: SmelterRecipe, input: TripleRecipeInput) { this@SmelteryEntity.input.forEach { getItem(it).shrink(recipe.count) } }
}
