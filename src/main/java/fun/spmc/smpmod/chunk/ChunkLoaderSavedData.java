package fun.spmc.smpmod.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.utils.MessageUtils;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChunkLoaderSavedData extends SavedData {
    private static final Codec<Set<BlockPos>> LOADERS_CODEC =
            BlockPos.CODEC.listOf().xmap(HashSet::new, ArrayList::new);

    public static final Codec<ChunkLoaderSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LOADERS_CODEC.fieldOf("active_loaders").forGetter(data -> data.activeLoaders)
    ).apply(instance, ChunkLoaderSavedData::new));

    public static final SavedDataType<ChunkLoaderSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "chunk_loaders"),
            ChunkLoaderSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Set<BlockPos> activeLoaders;
    private boolean suspended = false;

    public ChunkLoaderSavedData(Set<BlockPos> activeLoaders) {
        this.activeLoaders = new HashSet<>(activeLoaders);
    }

    public ChunkLoaderSavedData() {
        this(new HashSet<>());
    }

    public static ChunkLoaderSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void addLoader(ServerLevel level, BlockPos pos) {
        if (activeLoaders.add(pos)) {
            this.setDirty();
            if (!suspended) {
                ChunkPos chunkPos = ChunkPos.containing(pos);
                level.setChunkForced(chunkPos.x(), chunkPos.z(), true);
            }
        }
    }

    public void removeLoader(ServerLevel level, BlockPos pos) {
        if (activeLoaders.remove(pos)) {
            this.setDirty();
            ChunkPos chunkPos = ChunkPos.containing(pos);
            boolean hasOtherLoadersInChunk = activeLoaders.stream().anyMatch(p -> ChunkPos.containing(p).equals(chunkPos));

            if (!hasOtherLoadersInChunk) level.setChunkForced(chunkPos.x(), chunkPos.z(), false);
        }
    }

    public boolean isLoader(BlockPos pos) {
        return activeLoaders.contains(pos);
    }

    public void suspendAll(ServerLevel level) {
        if (suspended) return;
        this.suspended = true;
        for (BlockPos pos : activeLoaders) {
            ChunkPos chunkPos = ChunkPos.containing(pos);
            level.setChunkForced(chunkPos.x(), chunkPos.z(), false);
        }
    }

    public void restoreAll(ServerLevel level) {
        if (!suspended) return;
        this.suspended = false;
        for (BlockPos pos : activeLoaders) {
            ChunkPos chunkPos = ChunkPos.containing(pos);
            level.setChunkForced(chunkPos.x(), chunkPos.z(), true);
        }
    }

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