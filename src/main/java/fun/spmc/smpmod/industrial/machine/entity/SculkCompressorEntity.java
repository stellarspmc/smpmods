package fun.spmc.smpmod.industrial.machine.entity;

import fun.spmc.smpmod.industrial.machine.abstr.BaseMachineEntity;
import fun.spmc.smpmod.industrial.recipe.CompressorRecipe;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SculkCompressorEntity extends BaseMachineEntity {
    private static final int[] SLOTS_INPUT = new int[]{0};
    private static final int[] SLOTS_OUTPUT = new int[]{1};

    private final RecipeManager.CachedCheck<SingleRecipeInput, CompressorRecipe> quickCheck;

    public SculkCompressorEntity(BlockPos pos, BlockState state) {
        super(PolymerIndustrial.SCULK_ENTITY, pos, state, 2); // 0: Input, 1: Output
        this.quickCheck = RecipeManager.createCheck(PolymerIndustrial.COMPRESSOR_TYPE);
    }

    public static void tick(ServerLevel level, SculkCompressorEntity entity) {
        boolean changed = false;
        ItemStack inputStack = entity.getItem(0);

        if (!inputStack.isEmpty()) {
            SingleRecipeInput input = new SingleRecipeInput(inputStack);
            RecipeHolder<CompressorRecipe> recipeHolder = entity.quickCheck.getRecipeFor(input, level).orElse(null);

            if (recipeHolder != null) {
                CompressorRecipe recipe = recipeHolder.value();
                ItemStack burnResult = recipe.result().create();

                if (entity.canOutput(burnResult)) {
                    entity.maxProgressTicks = recipe.processTime();
                    entity.progressTicks++;

                    if (entity.progressTicks >= entity.maxProgressTicks) {
                        entity.progressTicks = 0;
                        inputStack.shrink(recipe.count());

                        ItemStack outputStack = entity.getItem(1);
                        if (outputStack.isEmpty()) entity.setItem(1, burnResult.copy());
                        else outputStack.grow(burnResult.getCount());
                        changed = true;
                    }
                } else entity.progressTicks = 0;
            } else entity.progressTicks = 0;
        } else if (entity.progressTicks > 0) {
            entity.progressTicks = 0;
            changed = true;
        }

        if (changed) setChanged(level, entity.getBlockPos(), entity.getBlockState());
    }

    private boolean canOutput(ItemStack recipeResult) {
        ItemStack currentOutput = getItem(1);
        if (currentOutput.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(currentOutput, recipeResult)) return false;
        return currentOutput.getCount() + recipeResult.getCount() <= Math.min(this.getMaxStackSize(), recipeResult.getMaxStackSize());
    }

    @Override public int @NonNull [] getSlotsForFace(@NonNull Direction direction) { return direction == Direction.DOWN ? SLOTS_OUTPUT : SLOTS_INPUT; }
    @Override public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack itemStack, @Nullable Direction direction) { return slot == 0; }
    @Override public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack itemStack, @NonNull Direction direction) { return slot == 1; }
}