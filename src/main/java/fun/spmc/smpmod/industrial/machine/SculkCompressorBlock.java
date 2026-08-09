package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import fun.spmc.smpmod.industrial.recipe.CompressorRecipe;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SculkCompressorBlock extends Block implements PolymerBlock {
    public static final MapCodec<SculkCompressorBlock> CODEC = simpleCodec(SculkCompressorBlock::new);
    public SculkCompressorBlock(Properties properties) { super(properties); }
    @Override public @NonNull MapCodec<? extends SculkCompressorBlock> codec() { return CODEC; }
    @Override public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) { return Blocks.SCULK_CATALYST.defaultBlockState().setValue(BlockStateProperties.BLOOM, true); }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) CompressorMenu.openCompressorUI(serverPlayer);
        return InteractionResult.SUCCESS;
    }

    static class CompressorMenu extends ChestMenu {
        public static final int INPUT_SLOT = 10;
        public static final int PROCESS_SLOT = 13;
        public static final int OUTPUT_SLOT = 16;

        private final Container container;
        private final Player player;
        private int progressTicks = 0;

        public CompressorMenu(int syncId, Inventory playerInventory, Container container) {
            super(MenuType.GENERIC_9x3, syncId, playerInventory, container, 3);
            this.container = container;
            this.player = playerInventory.player;

            for (int slotIndex = 0; slotIndex < 27; slotIndex++) {
                if (slotIndex == INPUT_SLOT) continue;

                if (slotIndex == OUTPUT_SLOT) {
                    this.slots.set(slotIndex, new Slot(container, slotIndex, (slotIndex % 9) * 18 + 8, (slotIndex / 9) * 18 + 18) {
                        @Override
                        public boolean mayPlace(@NonNull ItemStack stack) {
                            return false;
                        }
                    });
                    continue;
                }

                this.slots.set(slotIndex, new Slot(container, slotIndex, (slotIndex % 9) * 18 + 8, (slotIndex / 9) * 18 + 18) {
                    @Override
                    public boolean mayPlace(@NonNull ItemStack stack) { return false; }
                    @Override
                    public boolean mayPickup(@NonNull Player player) { return false; }
                });
            }
        }

        @Override
        public void broadcastChanges() {
            super.broadcastChanges();

            if (!this.player.level().isClientSide()) tickRecipe();
        }

        private void tickRecipe() {
            ItemStack inputStack = this.container.getItem(INPUT_SLOT);
            if (inputStack.isEmpty()) {
                resetProgress();
                return;
            }

            SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
            Optional<RecipeHolder<CompressorRecipe>> match = this.player.level()
                    .recipeAccess()
                    .getSynchronizedRecipes().getFirstMatch(PolymerIndustrial.COMPRESSOR_TYPE, recipeInput, this.player.level());

            if (match.isEmpty()) {
                resetProgress();
                return;
            }

            CompressorRecipe recipe = match.get().value();
            ItemStack outputStack = this.container.getItem(OUTPUT_SLOT);
            if (!canOutput(outputStack, recipe.result())) {
                resetProgress();
                return;
            }

            progressTicks++;
            updateProgressDisplay(progressTicks, recipe.processTime());

            if (progressTicks >= recipe.processTime()) {
                progressTicks = 0;

                inputStack.shrink(recipe.count());
                this.container.setItem(INPUT_SLOT, inputStack);

                if (outputStack.isEmpty()) {
                    this.container.setItem(OUTPUT_SLOT, recipe.result().copy());
                } else {
                    outputStack.grow(recipe.result().getCount());
                    this.container.setItem(OUTPUT_SLOT, outputStack);
                }
            }
        }

        private boolean canOutput(ItemStack currentOutput, ItemStack recipeResult) {
            if (currentOutput.isEmpty()) return true;
            if (!ItemStack.isSameItemSameComponents(currentOutput, recipeResult)) return false;
            return currentOutput.getCount() + recipeResult.getCount() <= currentOutput.getMaxStackSize();
        }

        private void resetProgress() {
            if (this.progressTicks != 0) {
                this.progressTicks = 0;
                updateProgressDisplay(0, 200);
            }
        }

        private void updateProgressDisplay(int current, int max) {
            ItemStack progressButton = new ItemStack(Items.POLISHED_BLACKSTONE_BUTTON);
            progressButton.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal("Compressing: " + (int) (((float) current / max) * 100) + "%")
                            .withColor(TextColor.fromRgb(0x55FFFF))
            );
            this.container.setItem(PROCESS_SLOT, progressButton);
        }

        @Override
        public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);

            if (slot != null && slot.hasItem()) {
                ItemStack stackInSlot = slot.getItem();
                itemstack = stackInSlot.copy();

                if (index < 27) if (!this.moveItemStackTo(stackInSlot, 27, 63, true)) return ItemStack.EMPTY;
                else if (!this.moveItemStackTo(stackInSlot, INPUT_SLOT, INPUT_SLOT + 1, false)) return ItemStack.EMPTY;

                if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
                else slot.setChanged();
            }
            return itemstack;
        }

        @Override
        public void removed(@NonNull Player player) {
            super.removed(player);
            if (!player.level().isClientSide()) {
                this.clearContainer(player, this.container);
            }
        }

        public static void openCompressorUI(ServerPlayer player) {
            SimpleContainer container = new SimpleContainer(27);

            ItemStack bgGlass = new ItemStack(Items.STAINED_GLASS_PANE.black());
            bgGlass.set(DataComponents.CUSTOM_NAME, Component.literal(""));

            for (int i = 0; i < 27; i++) {
                if (i != INPUT_SLOT && i != OUTPUT_SLOT) {
                    container.setItem(i, bgGlass);
                }
            }

            player.openMenu(new SimpleMenuProvider(
                    (syncId, inv, _) -> new CompressorMenu(syncId, inv, container),
                    Component.literal("Sculk Compressor").withColor(TextColor.fromRgb(0x00AAAA))
            ));
        }
    }
}