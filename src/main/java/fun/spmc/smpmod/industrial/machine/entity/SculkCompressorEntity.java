package fun.spmc.smpmod.industrial.machine.entity;

import fun.spmc.smpmod.industrial.machine.abstr.BaseMachineEntity;
import fun.spmc.smpmod.industrial.recipe.CompressorRecipe;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.state.BlockState;

public class SculkCompressorEntity extends BaseMachineEntity<SingleRecipeInput, CompressorRecipe> {
    public SculkCompressorEntity(BlockPos pos, BlockState state) {
        super(PolymerIndustrial.SCULK_ENTITY, pos, state, 2, PolymerIndustrial.COMPRESSOR_TYPE, new int[]{0}, new int[]{1}); // 0: input, 1: output
    }

    @Override
    protected void onProcessTick(ServerLevel level, int currentProgress, int maxProgress) {
        if (currentProgress % 6 == 0) {
            float pitch = 0.6f + (((float) currentProgress / maxProgress) * .8f);
            level.playSound(null, this.worldPosition, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, .4f, pitch);
        }
    }

    @Override
    protected void onProcessComplete(ServerLevel level, ItemStack result) {
        level.playSound(null, this.worldPosition, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, .8f, 1.2f);
        level.playSound(null, this.worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, .3f, .5f);
        level.sendParticles(ParticleTypes.SCULK_SOUL, this.worldPosition.getX() + .5, this.worldPosition.getY() + 1.1, this.worldPosition.getZ() + .5, 15, .25, .25, .25, .03);
    }

    @Override
    protected SingleRecipeInput createRecipeInput() {
        ItemStack stack = getItem(input[0]);
        return stack.isEmpty() ? null : new SingleRecipeInput(stack);
    }

    @Override protected ItemStack getRecipeResult(CompressorRecipe recipe, ServerLevel level) { return recipe.result().create(); }
    @Override protected int getRecipeProcessTime(CompressorRecipe recipe) { return recipe.processTime(); }
    @Override protected void consumeIngredients(CompressorRecipe recipe, SingleRecipeInput recipeInput) { getItem(input[0]).shrink(recipe.count()); }
}