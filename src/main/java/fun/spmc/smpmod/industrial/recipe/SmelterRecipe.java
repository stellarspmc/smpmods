package fun.spmc.smpmod.industrial.recipe;

import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SmelterRecipe(List<Ingredient> ingredients, int count, ItemStack result, int processTime) implements Recipe<SmelterRecipe.TripleRecipeInput> {
    @Override public @NonNull ItemStack assemble(SmelterRecipe.@NonNull TripleRecipeInput input) { return this.result.copy(); }
    @Override public boolean showNotification() { return false; }
    @Override public @NonNull String group() { return ""; }
    @Override public @NonNull RecipeSerializer<? extends Recipe<TripleRecipeInput>> getSerializer() { return PolymerIndustrial.SMELTERY_SERIALIZER; }
    @Override public @NonNull RecipeType<? extends Recipe<TripleRecipeInput>> getType() { return PolymerIndustrial.SMELTERY_TYPE; }
    @Override public @NonNull PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public @Nullable RecipeBookCategory recipeBookCategory() { return null; }
    @Override public boolean matches(TripleRecipeInput input, @NonNull Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getCount() < this.count) return false;
                inputs.add(stack);
            }
        }

        if (inputs.size() != this.ingredients.size()) return false;
        boolean[] matched = new boolean[inputs.size()];
        for (Ingredient ingredient : this.ingredients) {
            boolean foundMatch = false;
            for (int i = 0; i < inputs.size(); i++) {
                if (!matched[i] && ingredient.test(inputs.get(i))) {
                    matched[i] = true;
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }

        return true;
    }

    public record TripleRecipeInput(ItemStack item1, ItemStack item2, ItemStack item3) implements RecipeInput {
        public @NonNull ItemStack getItem(final int index) {
            return switch (index) {
                case 0 -> item1;
                case 1 -> item2;
                case 2 -> item3;
                default -> throw new IllegalArgumentException("No item for index " + index);
            };
        }

        public int size() { return 3; }
    }
}
