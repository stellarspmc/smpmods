package spmc.smpmod.treasure

import net.minecraft.world.level.ChunkPos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

object ChunkPool {
    private val chunkBasedPool = HashMap<ChunkPos, Double>()

    fun increment(chunk: ChunkPos) {
        val original = chunkBasedPool.getOrDefault(chunk, .0)
        chunkBasedPool[chunk] = original + max(.0, 4 / ln(original + Math.E))
    }

	fun multiplier(chunk: ChunkPos): Double =  1 - (chunkBasedPool.getOrDefault(chunk, .0) / 275)
    fun serverTickLoop() = apply { for (pos in chunkBasedPool.keys) chunkBasedPool.replace(pos, Math.clamp(chunkBasedPool.getOrDefault(pos, 0.0).pow(.999), 0.0, 100.0)) }
    fun checkChunkPool(pos: ChunkPos): Boolean = chunkBasedPool.getOrDefault(pos, .0) <= 100.0
}
