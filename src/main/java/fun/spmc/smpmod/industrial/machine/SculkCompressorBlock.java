package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.industrial.recipe.CompressorRecipe;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
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
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) new CompressorUI(serverPlayer).open();
        return InteractionResult.SUCCESS;
    }

    static class CompressorUI extends SimpleGui {
        public static final int INPUT_SLOT = 10;
        public static final int PROCESS_SLOT = 13;
        public static final int OUTPUT_SLOT = 16;
        private int progressTicks = 0;

        public CompressorUI(ServerPlayer player) {
            super(MenuType.GENERIC_9x3, player, false);
            this.setTitle(Component.literal("Sculk Compressor").withColor(TextColor.fromRgb(0x00AAAA)));

            for (int i = 0; i < 27; i++) this.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")));

            this.clearSlot(INPUT_SLOT);
            this.clearSlot(OUTPUT_SLOT);
            updateProgressDisplay(0, 200);
            setLockPlayerInventory(false);
        }

        @Override
        public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
            if (index == OUTPUT_SLOT && (action == ContainerInput.PICKUP_ALL || action == ContainerInput.PICKUP || action == ContainerInput.SWAP)) return false;
            return super.onAnyClick(index, type, action);
        }

        @Override
        public void onTick() {
            Slot inputSlot = this.getCustomSlot(INPUT_SLOT);
            Slot outputSlot = this.getCustomSlot(OUTPUT_SLOT);

            if (inputSlot == null || outputSlot == null) {
                return;
            }

            ItemStack inputStack = inputSlot.getItem();
            if (inputStack.isEmpty()) {
                resetProgress();
                return;
            }

            SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
            Optional<RecipeHolder<CompressorRecipe>> match = this.player.level()
                    .recipeAccess()
                    .getRecipeFor(PolymerIndustrial.COMPRESSOR_TYPE, recipeInput, this.player.level());

            if (match.isEmpty()) {
                resetProgress();
                return;
            }

            CompressorRecipe recipe = match.get().value();
            ItemStack outputStack = outputSlot.getItem();
            if (!canOutput(outputStack, recipe.result())) {
                resetProgress();
                return;
            }

            progressTicks++;
            updateProgressDisplay(progressTicks, recipe.processTime());

            if (progressTicks >= recipe.processTime()) {
                progressTicks = 0;

                inputStack.shrink(recipe.count());
                inputSlot.setChanged();

                if (outputStack.isEmpty()) outputSlot.set(recipe.result().copy());
                else {
                    outputStack.grow(recipe.result().getCount());
                    outputSlot.setChanged();
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
            this.setSlot(PROCESS_SLOT, new GuiElementBuilder(Items.POLISHED_BLACKSTONE_BUTTON)
                    .setName(Component.literal("Compressing: " + (int) (((float) current / max) * 100) + "%")
                            .withColor(TextColor.fromRgb(0x55FFFF))));
        }
    }
}