package spmc.smpmod.mining

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import spmc.smpmod.core.BiomeCategory
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.mining.ChunkPool.checkChunkPool
import spmc.smpmod.mining.ChunkPool.increment
import spmc.smpmod.mining.ChunkPool.multiplier
import spmc.smpmod.mining.TreasureSpawner.spawnTreasureContainer
import spmc.smpmod.utils.anyInCreative
import java.util.*
import kotlin.enums.enumEntries
import kotlin.jvm.optionals.getOrNull
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

object TreasureHelper {
	@JvmField var eventPercentage: Double = 1.0
	@JvmField var rigTreasures: Boolean = false

	private val THRESHOLD = listOf(.0001, .0005, .0024, .007, .03, .12, .28, .56)
	private fun getThreshold(index: Int) = THRESHOLD[index]


    fun onBlockBreak(world: Level, player: Player, pos: BlockPos, state: BlockState, @Suppress("unused") ignored: BlockEntity?) {
        if (world.isClientSide) return
	    if (anyInCreative(player as ServerPlayer)) return

        val mainHand = player.mainHandItem
        val enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val silkTouchHolder = enchantmentRegistry.get(Enchantments.SILK_TOUCH)
        if (silkTouchHolder.isPresent && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), mainHand) > 0) return
	    val fortune = enchantmentRegistry.get(Enchantments.FORTUNE).getOrNull()?.let { EnchantmentHelper.getItemEnchantmentLevel(it, mainHand) } ?: 0

        val biomes = BiomeCategory.getGroup(world.getBiome(pos))
        val rarity = rollTreasureRarity(state, eventPercentage * multiplier(ChunkPos.containing(pos)) * (1f + (ln(fortune + 1f) + 1f) / 8f), world.getRandom()) ?: return

        if (checkChunkPool(ChunkPos.containing(pos))) return
        increment(ChunkPos.containing(pos))
        spawnTreasureContainer(world as ServerLevel, pos, rarity, player, biomes)
    }

    private fun rollTreasureRarity(state: BlockState, multiplier: Double, random: RandomSource): ItemRarity? {
        val commonChance = (getBaseCommonChance(state) * multiplier).toFloat()
        if (commonChance <= 0) return null
	    if ((random.nextFloat() * 100f) > commonChance) return null
	    val roll = random.nextDouble()
	    var cumulative = .0
	    for (rarity in ItemRarity.entries) {
		    cumulative += getThreshold(7 - rarity.ordinal)
		    if (roll <= cumulative) return adjustRarity(rarity)
	    }
	    return null
    }

    private fun getBaseCommonChance(state: BlockState): Float = BlockRates.getMultiplier(state)
	private fun adjustRarity(rarity: ItemRarity): ItemRarity {
		if (!rigTreasures || rarity.ordinal >= ItemRarity.entries.lastIndex) return rarity
		return ItemRarity.entries[rarity.ordinal + 1]
	}

    internal enum class BlockRates(val multiplier: Float, private val blocks: MutableList<Block>) { // TODO: new blocks?
        VERY_HIGH(2f, mutableListOf(
	        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
	        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
	        Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ANCIENT_DEBRIS, Blocks.SCULK_SHRIEKER
		)),
        HIGH(1.3f, mutableListOf(
	        Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_COPPER_ORE,
	        Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE
		)),
        MID(1.1f, mutableListOf(
	        Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE,
	        Blocks.GOLD_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE,
	        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE
		)),
        LOW(.85f, mutableListOf(
	        Blocks.STONE, Blocks.TUFF, Blocks.ANDESITE, Blocks.GRANITE,
	        Blocks.AMETHYST_BLOCK, Blocks.DRIPSTONE_BLOCK, Blocks.DIORITE, Blocks.DEEPSLATE,
	        Blocks.BASALT, Blocks.BLACKSTONE, Blocks.SMOOTH_BASALT, Blocks.MAGMA_BLOCK, Blocks.END_STONE, Blocks.CINNABAR, Blocks.SULFUR
		)),
        VERY_LOW(.3f, mutableListOf(Blocks.CALCITE, Blocks.SANDSTONE, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.GRAVEL));

        companion object {
            private val RATE_MAP = IdentityHashMap<Block, Float>().apply { enumEntries<BlockRates>().forEach { entry -> entry.blocks.forEach { put(it, entry.multiplier) }} }
            fun getMultiplier(state: BlockState) = RATE_MAP[state.block] ?: 0f
        }
    }
}

object ChunkPool {
	private val chunkBasedPool = HashMap<ChunkPos, Double>()

	fun increment(chunk: ChunkPos) {
		val original = chunkBasedPool.getOrDefault(chunk, .0)
		chunkBasedPool[chunk] = original + max(.0, 4 / ln(original + Math.E))
	}

	fun multiplier(chunk: ChunkPos) =  1 - (chunkBasedPool.getOrDefault(chunk, .0) / 275)
	fun serverTickLoop() { for (pos in chunkBasedPool.keys) chunkBasedPool.replace(pos, Math.clamp(chunkBasedPool.getOrDefault(pos, .0).pow(.999), .0, 100.0)) }
	fun checkChunkPool(pos: ChunkPos) = chunkBasedPool.getOrDefault(pos, .0) > 100.0
}