package fun.spmc.smpmod.utils;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PolymerEntityBlock extends BaseEntityBlock implements PolymerBlock {
    private final BlockState state;
    private final BlockEntity entity;

    protected PolymerEntityBlock(Properties properties, BlockState state, BlockEntity blockEntity) {
        super(properties);
        this.state = state;
        entity = blockEntity;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return state;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec((properties) -> new PolymerEntityBlock(properties, state, entity));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos worldPosition, @NonNull BlockState blockState) {
        return entity;
    }

    public BlockState getState() {
        return state;
    }
}
