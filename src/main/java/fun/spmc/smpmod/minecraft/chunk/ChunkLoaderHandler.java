package fun.spmc.smpmod.minecraft.chunk;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
                    player.sendSystemMessage(Component.literal(":anchor: ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("chunk loader activated.").withStyle(ChatFormatting.GRAY)));
                }
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!level.isClientSide() && state.is(Blocks.LODESTONE)) {
                ServerLevel serverLevel = (ServerLevel) level;
                ChunkLoaderSavedData data = ChunkLoaderSavedData.get(serverLevel);

                if (data.isLoader(pos)) {
                    data.removeLoader(serverLevel, pos);
                    player.sendSystemMessage(Component.literal(":warning: ")
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal("chunk loader deactivated.").withStyle(ChatFormatting.GRAY)));
                }
            }
            return true;
        });
    }
}