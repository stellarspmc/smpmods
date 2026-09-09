package `fun`.spmc.smpmod.treasure

import net.minecraft.world.level.ChunkPos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

object ChunkPool {
    private val chunkBasedPool = HashMap<ChunkPos, Double>()

    @JvmStatic
    fun increment(chunk: ChunkPos) {
        val original = chunkBasedPool.getOrDefault(chunk, 0.0)
        chunkBasedPool[chunk] = original + max(0.0, 4 / ln(original + Math.E))
    }

    @JvmStatic fun serverTickLoop() { for (pos in chunkBasedPool.keys) chunkBasedPool.replace(pos, Math.clamp(chunkBasedPool.getOrDefault(pos, 0.0).pow(.999), 0.0, 100.0)) }
    @JvmStatic fun checkChunkPool(pos: ChunkPos): Boolean { return chunkBasedPool.getOrDefault(pos, 0.0) <= 100.0 }
}
