package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.industrial.machine.abstr.BaseMachineBlock;
import fun.spmc.smpmod.industrial.machine.entity.SculkCompressorEntity;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SculkCompressorBlock extends BaseMachineBlock<SculkCompressorEntity> {
    public static final MapCodec<SculkCompressorBlock> CODEC = simpleCodec(SculkCompressorBlock::new);

    public SculkCompressorBlock(Properties properties) { super(properties, () -> PolymerIndustrial.SCULK_ENTITY, SculkCompressorEntity::new); }
    @Override protected @NonNull MapCodec<? extends SculkCompressorBlock> codec() { return CODEC; }
    @Override public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) { return Blocks.SCULK_CATALYST.defaultBlockState().setValue(BlockStateProperties.BLOOM, true); }

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
            int progress = blockEntity.getProgressTicks();
            int percent = (int) (((float) progress / max) * 100);

            if (progress > 0) {
                int filledBars = percent / 10;
                Component progressBar = Component.empty()
                        .append(Component.literal("█".repeat(filledBars)).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("▒".repeat(10 - filledBars)).withStyle(ChatFormatting.GRAY));

                Component progressText = Component.literal("Progress: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(percent + "%").withStyle(ChatFormatting.YELLOW));

                this.setSlot(PROCESS_SLOT, new GuiElementBuilder(Items.ECHO_SHARD)
                        .setName(Component.literal("Compressing...")
                                .withColor(TextColor.fromRgb(0x00AAAA))
                                .withStyle(ChatFormatting.BOLD))
                        .addLoreLine(progressBar)
                        .addLoreLine(progressText)
                        .setCount(Math.max(1, (percent * 64) / 100))
                        .glow());
            } else {
                this.setSlot(PROCESS_SLOT, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                        .setName(Component.literal("Waiting for Input...")
                                .withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}