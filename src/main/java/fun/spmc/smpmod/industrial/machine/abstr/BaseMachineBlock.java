package fun.spmc.smpmod.industrial.machine.abstr;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public abstract class BaseMachineBlock<E extends BaseMachineEntity> extends BaseEntityBlock implements PolymerBlock {
    private final Supplier<BlockEntityType<E>> blockEntityTypeSupplier;
    private final BlockEntityType.BlockEntitySupplier<E> blockEntityFactory;

    protected BaseMachineBlock(Properties properties, Supplier<BlockEntityType<E>> blockEntityTypeSupplier, BlockEntityType.BlockEntitySupplier<E> blockEntityFactory) {
        super(properties.requiresCorrectToolForDrops().strength(3.5f).mapColor(MapColor.STONE));
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.blockEntityFactory = blockEntityFactory;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        if (level instanceof ServerLevel serverLevel) return createTickerHelper(type, this.blockEntityTypeSupplier.get(), (_, _, _, entity) -> entity.serverTick(serverLevel));
        return null;
    }

    @Override public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) { return this.blockEntityFactory.create(pos, state); }
    @Override protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) { return RenderShape.MODEL; }
    @Override protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) { Containers.updateNeighboursAfterDestroy(state, level, pos); }
}