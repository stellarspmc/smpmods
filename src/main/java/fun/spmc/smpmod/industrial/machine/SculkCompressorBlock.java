package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.industrial.machine.abstr.BaseMachineBlock;
import fun.spmc.smpmod.industrial.machine.entity.SculkCompressorEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SculkCompressorBlock extends BaseMachineBlock {
    public static final MapCodec<SculkCompressorBlock> CODEC = simpleCodec(SculkCompressorBlock::new);

    public SculkCompressorBlock(Properties properties) { super(properties); }
    @Override protected @NonNull MapCodec<? extends BaseMachineBlock> codec() { return CODEC; }
    @Override public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) { return Blocks.SCULK_CATALYST.defaultBlockState().setValue(BlockStateProperties.BLOOM, true); }
    @Nullable @Override public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) { return new SculkCompressorEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) { return createMachineTicker(level, type, PolymerIndustrial.SCULK_ENTITY, SculkCompressorEntity::tick); }


    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof SculkCompressorEntity compressor) new CompressorUI(serverPlayer, compressor).open();
        return InteractionResult.SUCCESS;
    }

    static class CompressorUI extends SimpleGui {
        public static final int INPUT_SLOT = 10;
        public static final int PROCESS_SLOT = 13;
        public static final int OUTPUT_SLOT = 16;

        private final SculkCompressorEntity blockEntity;

        public CompressorUI(ServerPlayer player, SculkCompressorEntity blockEntity) {
            super(MenuType.GENERIC_9x3, player, false);
            this.blockEntity = blockEntity;
            this.setTitle(Component.literal("Sculk Compressor").withColor(TextColor.fromRgb(0x00AAAA)));

            for (int i = 0; i < 27; i++) this.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")));
            this.setSlot(INPUT_SLOT, new Slot(blockEntity, 0, 0, 0));
            this.setSlot(OUTPUT_SLOT, new Slot(blockEntity, 1, 0, 0) { @Override public boolean mayPlace(@NonNull ItemStack stack) { return false; } });
            setLockPlayerInventory(false);
        }

        @Override
        public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
            if (index == OUTPUT_SLOT && (action == ContainerInput.PICKUP_ALL || action == ContainerInput.PICKUP || action == ContainerInput.SWAP)) return false;
            return super.onAnyClick(index, type, action);
        }

        @Override
        public void onTick() {
            int max = Math.max(1, blockEntity.getMaxProgressTicks());
            int progress = (int) (((float) blockEntity.getProgressTicks() / max) * 100);

            this.setSlot(PROCESS_SLOT, new GuiElementBuilder(Items.POLISHED_BLACKSTONE_BUTTON)
                    .setName(Component.literal("Compressing: " + progress + "%").withColor(TextColor.fromRgb(0x55FFFF))));
        }
    }
}