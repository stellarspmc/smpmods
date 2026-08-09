package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import fun.spmc.smpmod.industrial.machine.entity.CompressorEntity;
import fun.spmc.smpmod.utils.PolymerEntityBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public class SculkCompressorBlock extends PolymerEntityBlock {
    protected SculkCompressorBlock(Properties properties, BlockState state) {
        super(properties, Blocks.SCULK_CATALYST.defaultBlockState().setValue(BlockStateProperties.BLOOM, true), new CompressorEntity());
    }
}