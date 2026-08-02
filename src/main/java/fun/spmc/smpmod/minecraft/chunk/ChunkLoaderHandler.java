package fun.spmc.smpmod.minecraft.chunk;

import fun.spmc.smpmod.minecraft.utils.MessageUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;

public class ChunkLoaderHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, _, hitResult) -> {
            if (level.isClientSide()) return InteractionResult.PASS;

            ServerLevel serverLevel = (ServerLevel) level;
            var pos = hitResult.getBlockPos();

            if (level.getBlockState(pos).is(Blocks.LODESTONE)) {
                ChunkLoaderSavedData data = ChunkLoaderSavedData.get(serverLevel);

                if (!data.isLoader(pos)) {
                    data.addLoader(serverLevel, pos);
                    MessageUtils.sendSuccessMessage((ServerPlayer) player, "Chunk loader activated.");
                }
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, _) -> {
            if (!level.isClientSide() && state.is(Blocks.LODESTONE)) {
                ServerLevel serverLevel = (ServerLevel) level;
                ChunkLoaderSavedData data = ChunkLoaderSavedData.get(serverLevel);

                if (data.isLoader(pos)) {
                    data.removeLoader(serverLevel, pos);
                    MessageUtils.sendErrorMessage((ServerPlayer) player, "Chunk loader deactivated.");
                }
            }
            return true;
        });

        ServerPlayConnectionEvents.JOIN.register((_, _, server) -> {
            if (server.getPlayerList().getPlayerCount() == 1)
                for (ServerLevel level : server.getAllLevels()) ChunkLoaderSavedData.get(level).restoreAll(level);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((_, server) -> {
            if (server.getPlayerList().getPlayerCount() <= 1)
                for (ServerLevel level : server.getAllLevels()) ChunkLoaderSavedData.get(level).suspendAll(level);
        });
    }
}