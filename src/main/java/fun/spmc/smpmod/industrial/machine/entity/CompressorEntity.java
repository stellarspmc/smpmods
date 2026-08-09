package fun.spmc.smpmod.industrial.machine.entity;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CompressorEntity extends BlockEntity implements PolymerSyncedObject<BlockEntity> {
    public CompressorEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Override
    public BlockEntity getPolymerReplacement(BlockEntity object, PacketContext context) {
        return null;
    }
}
