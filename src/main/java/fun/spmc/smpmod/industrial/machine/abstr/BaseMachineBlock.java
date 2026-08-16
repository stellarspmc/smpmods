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
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

public abstract class BaseMachineBlock extends BaseEntityBlock implements PolymerBlock {
    protected BaseMachineBlock(Properties properties) { super(properties); }
    @Override protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) { return RenderShape.MODEL; }
    @Override protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) { Containers.updateNeighboursAfterDestroy(state, level, pos); }

    protected static <T extends BlockEntity, E extends BaseMachineEntity> BlockEntityTicker<T> createMachineTicker(Level level, BlockEntityType<T> actualType, BlockEntityType<E> expectedType, BiConsumer<ServerLevel, E> tickConsumer) {
        if (level instanceof ServerLevel serverLevel) return createTickerHelper(actualType, expectedType, (_, _, _, entity) -> tickConsumer.accept(serverLevel, entity));
        return null;
    }
}