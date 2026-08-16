package fun.spmc.smpmod.industrial.machine.abstr;

import fun.spmc.smpmod.industrial.machine.entity.SculkCompressorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public abstract class BaseMachineEntity extends BlockEntity implements WorldlyContainer {
    protected NonNullList<ItemStack> items;
    protected int progressTicks = 0;
    protected int maxProgressTicks = 500;

    protected BaseMachineEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int containerSize) {
        super(type, pos, state);
        this.items = NonNullList.withSize(containerSize, ItemStack.EMPTY);
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
    public int getContainerSize() {
        return this.items.size();
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

    @Override public @NonNull ItemStack getItem(int slot) { return this.items.get(slot); }
    @Override public @NonNull ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(this.items, slot, amount); }
    @Override public @NonNull ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(this.items, slot); }
    @Override public boolean stillValid(@NonNull Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { this.items.clear(); }
    public int getProgressTicks() { return progressTicks; }
    public int getMaxProgressTicks() { return maxProgressTicks; }
}
