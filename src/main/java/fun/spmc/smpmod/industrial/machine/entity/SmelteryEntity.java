package fun.spmc.smpmod.industrial.machine.entity;

import fun.spmc.smpmod.industrial.machine.abstr.BaseMachineEntity;
import fun.spmc.smpmod.industrial.recipe.SmelterRecipe;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class SmelteryEntity extends BaseMachineEntity<SmelterRecipe.TripleRecipeInput, SmelterRecipe> {
    public SmelteryEntity(BlockPos pos, BlockState state) { super(PolymerIndustrial.SMELTERY_ENTITY, pos, state, 4, PolymerIndustrial.SMELTERY_TYPE, new int[]{0, 1, 2}, new int[]{3}); }

    @Override
    protected void onProcessTick(ServerLevel level, int currentProgress, int maxProgress) {

    }

    @Override
    protected void onProcessComplete(ServerLevel level, ItemStack result) {

    }

    @Override
    protected SmelterRecipe.TripleRecipeInput createRecipeInput() {
        ItemStack in1 = getItem(0);
        ItemStack in2 = getItem(1);
        ItemStack in3 = getItem(2);

        if (in1.isEmpty() && in2.isEmpty() && in3.isEmpty()) return null;
        return new SmelterRecipe.TripleRecipeInput(in1, in2, in3);
    }

    @Override protected ItemStack getRecipeResult(SmelterRecipe recipe, ServerLevel level) { return recipe.result().copy(); }
    @Override protected int getRecipeProcessTime(SmelterRecipe recipe) { return recipe.processTime(); }
    @Override protected void consumeIngredients(SmelterRecipe recipe, SmelterRecipe.TripleRecipeInput recipeInput) { Arrays.stream(input).forEach((i) -> getItem(i).shrink(recipe.count())); }
}
