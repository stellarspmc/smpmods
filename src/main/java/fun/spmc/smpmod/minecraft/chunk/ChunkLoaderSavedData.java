package fun.spmc.smpmod.minecraft.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
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
            Identifier.fromNamespaceAndPath("smpmods", "chunk_loaders"),
            ChunkLoaderSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Set<BlockPos> activeLoaders;

    public ChunkLoaderSavedData(Set<BlockPos> activeLoaders) {
        this.activeLoaders = new HashSet<>(activeLoaders);
    }

    public ChunkLoaderSavedData() {
        this(new HashSet<>());
    }

    public static ChunkLoaderSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean addLoader(ServerLevel level, BlockPos pos) {
        if (activeLoaders.add(pos)) {
            this.setDirty();
            // Uses ChunkPos.containing() to turn BlockPos -> ChunkPos
            ChunkPos chunkPos = ChunkPos.containing(pos);
            level.setChunkForced(chunkPos.x(), chunkPos.z(), true);
            return true;
        }
        return false;
    }

    public boolean removeLoader(ServerLevel level, BlockPos pos) {
        if (activeLoaders.remove(pos)) {
            this.setDirty();
            ChunkPos chunkPos = ChunkPos.containing(pos);

            // Checks if any remaining loader block is in the same chunk
            boolean hasOtherLoadersInChunk = activeLoaders.stream()
                    .anyMatch(p -> ChunkPos.containing(p).equals(chunkPos));

            if (!hasOtherLoadersInChunk) {
                level.setChunkForced(chunkPos.x(), chunkPos.z(), false);
            }
            return true;
        }
        return false;
    }

    public boolean isLoader(BlockPos pos) {
        return activeLoaders.contains(pos);
    }
}