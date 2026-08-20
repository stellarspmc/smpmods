package fun.spmc.smpmod.industrial.machine.abstr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public abstract class BaseMachineEntity<I extends RecipeInput, R extends Recipe<I>> extends BlockEntity implements WorldlyContainer {
    protected NonNullList<ItemStack> items;
    protected int progressTicks = 0;
    protected int maxProgressTicks = 500;
    private final RecipeManager.CachedCheck<I, R> quickCheck;
    protected final int[] input;
    protected final int[] output;

    protected BaseMachineEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int containerSize, RecipeType<R> recipeType, int[] input, int[] output) {
        super(type, pos, state);
        this.items = NonNullList.withSize(containerSize, ItemStack.EMPTY);
        this.quickCheck = RecipeManager.createCheck(recipeType);
        this.input = input;
        this.output = output;
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progressTicks = input.getShortOr("progress_ticks", (short) 0);
        this.maxProgressTicks = input.getShortOr("max_progress_ticks", (short) 500);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putShort("progress_ticks", (short) this.progressTicks);
        output.putShort("max_progress_ticks", (short) this.maxProgressTicks);
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    public void serverTick(ServerLevel level) {
        boolean changed = false;
        I input = createRecipeInput();

        if (input != null && !input.isEmpty()) {
            RecipeHolder<R> recipeHolder = quickCheck.getRecipeFor(input, level).orElse(null);

            if (recipeHolder != null) {
                R recipe = recipeHolder.value();
                ItemStack resultStack = getRecipeResult(recipe, level);

                if (canOutput(resultStack)) {
                    this.maxProgressTicks = getRecipeProcessTime(recipe);
                    this.progressTicks++;

                    onProcessTick(level, this.progressTicks, this.maxProgressTicks);

                    if (this.progressTicks >= this.maxProgressTicks) {
                        this.progressTicks = 0;

                        consumeIngredients(recipe, input);
                        produceOutput(resultStack);
                        onProcessComplete(level, resultStack);
                        changed = true;
                    }
                } else resetProgress();
            } else resetProgress();
        } else if (this.progressTicks > 0) {
            resetProgress();
            changed = true;
        }

        if (changed) setChanged(level, this.getBlockPos(), this.getBlockState());
    }

    protected boolean canOutput(ItemStack recipeResult) {
        if (recipeResult.isEmpty()) return true;
        for (int slot : output) {
            ItemStack current = getItem(slot);
            if (current.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(current, recipeResult)
                    && current.getCount() + recipeResult.getCount() <= Math.min(getMaxStackSize(), recipeResult.getMaxStackSize()))
                return true;
        }
        return false;
    }

    protected void produceOutput(ItemStack recipeResult) {
        if (recipeResult.isEmpty()) return;
        for (int slot : output) {
            ItemStack current = getItem(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, recipeResult)) {
                int maxCount = Math.min(getMaxStackSize(), recipeResult.getMaxStackSize());
                if (current.getCount() + recipeResult.getCount() <= maxCount) {
                    current.grow(recipeResult.getCount());
                    return;
                }
            }
        }

        for (int slot : output) {
            ItemStack current = getItem(slot);
            if (current.isEmpty()) {
                setItem(slot, recipeResult.copy());
                return;
            }
        }
    }
    
    protected abstract void onProcessTick(ServerLevel level, int currentProgress, int maxProgress);
    protected abstract void onProcessComplete(ServerLevel level, ItemStack result);
    protected abstract @Nullable I createRecipeInput();
    protected abstract ItemStack getRecipeResult(R recipe, ServerLevel level);
    protected abstract int getRecipeProcessTime(R recipe);
    protected abstract void consumeIngredients(R recipe, I input);

    @Override public @NonNull ItemStack getItem(int slot) { return this.items.get(slot); }
    @Override public @NonNull ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(this.items, slot, amount); }
    @Override public @NonNull ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(this.items, slot); }
    @Override public boolean stillValid(@NonNull Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public int @NonNull [] getSlotsForFace(@NonNull Direction direction) { return direction == Direction.DOWN ? output : input; }
    @Override public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack itemStack, @Nullable Direction direction) { return Arrays.stream(input).anyMatch((a) -> Objects.equals(a, slot)); }
    @Override public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack itemStack, @NonNull Direction direction) { return Arrays.stream(output).anyMatch((a) -> Objects.equals(a, slot)); }
    @Override public void clearContent() { this.items.clear(); }
    @Override public int getContainerSize() { return this.items.size(); }
    public int getProgressTicks() { return progressTicks; }
    public int getMaxProgressTicks() { return maxProgressTicks; }
    private void resetProgress() { if (this.progressTicks > 0) this.progressTicks = 0; }
}
