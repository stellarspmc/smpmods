package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SmelteryBlock extends Block implements PolymerBlock {
    public static final MapCodec<SmelteryBlock> CODEC = simpleCodec(SmelteryBlock::new);

    public SmelteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull MapCodec<? extends SmelteryBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return Blocks.SMOKER.defaultBlockState().setValue(BlockStateProperties.LIT, true);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(Component.literal("The smeltry is a work in progress..."));//new SmeltryUI(serverPlayer).open();
        return InteractionResult.SUCCESS;
    }

}