package fun.spmc.smpmod.industrial.recipe;

import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record CompressorRecipe(Ingredient ingredient, int count, ItemStack result, int processTime) implements Recipe<SingleRecipeInput> {
    @Override public @NonNull ItemStack assemble(@NonNull SingleRecipeInput input) { return this.result.copy(); }
    @Override public boolean showNotification() { return false; }
    @Override public @NonNull String group() { return ""; }
    @Override public @NonNull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() { return PolymerIndustrial.COMPRESSOR_SERIALIZER; }
    @Override public @NonNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() { return PolymerIndustrial.COMPRESSOR_TYPE; }
    @Override public @NonNull PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public @Nullable RecipeBookCategory recipeBookCategory() { return null; }
    @Override public boolean matches(SingleRecipeInput input, @NonNull Level level) {
        ItemStack stack = input.getItem(0);
        return this.ingredient.test(stack) && stack.getCount() >= this.count;
    }
}
