package `fun`.spmc.smpmod.treasure

import `fun`.spmc.smpmod.core.ItemRarity
import net.minecraft.resources.ResourceKey
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.enums.enumEntries

object TreasureHelper {
    @JvmField var rigTreasures: Boolean = false

    private const val THRESHOLD_CELESTIAL = .0001f // .0001%
    private const val THRESHOLD_CHROMATIC = THRESHOLD_CELESTIAL + .0005f // .0005%
    private const val THRESHOLD_MYTHIC = THRESHOLD_CHROMATIC + .0024f // .0024%
    private const val THRESHOLD_LEGENDARY = THRESHOLD_MYTHIC + .007f // .007%
    private const val THRESHOLD_EPIC = THRESHOLD_LEGENDARY + .03f // .03%
    private const val THRESHOLD_RARE = THRESHOLD_EPIC + .12f // .12%
    private const val THRESHOLD_UNCOMMON = THRESHOLD_RARE + .28f // .28%
    private const val THRESHOLD_COMMON = THRESHOLD_UNCOMMON + .56f // .56%

    fun rollTreasureRarity(state: BlockState, multiplier: Double, random: RandomSource): ItemRarity? {
        val commonChance = (getBaseCommonChance(state) * multiplier).toFloat()
        if (commonChance <= 0) return null
        val roll = (random.nextFloat() * 100f) / commonChance
        if (roll < THRESHOLD_CELESTIAL) return ItemRarity.ASTRAL
        if (roll < THRESHOLD_CHROMATIC) return adjustRarity(ItemRarity.CHROMATIC)
        if (roll < THRESHOLD_MYTHIC) return adjustRarity(ItemRarity.MYTHIC)
        if (roll < THRESHOLD_LEGENDARY) return adjustRarity(ItemRarity.LEGENDARY)
        if (roll < THRESHOLD_EPIC) return adjustRarity(ItemRarity.EPIC)
        if (roll < THRESHOLD_RARE) return adjustRarity(ItemRarity.RARE)
        if (roll < THRESHOLD_UNCOMMON) return adjustRarity(ItemRarity.UNCOMMON)
        if (roll < THRESHOLD_COMMON) return adjustRarity(ItemRarity.COMMON)
        return null
    }

    private fun getBaseCommonChance(state: BlockState): Float { return BlockRates.getMultiplier(state) }

    private fun adjustRarity(rarity: ItemRarity): ItemRarity {
        if (!rigTreasures || rarity.ordinal == ItemRarity.entries.size) return rarity
        return ItemRarity.entries[rarity.ordinal + 1]
    }

    enum class Biomes(private val biomes: MutableList<String>) {
        BADLANDS(mutableListOf("badlands")),
        DESERT(mutableListOf("desert")),
        DRIP(mutableListOf("dripstone")),
        DARK_FOREST(mutableListOf("dark_forest")),
        SCULK(mutableListOf("deep_dark")),
        LUSH(mutableListOf("lush_caves")),
        MUSHROOM(mutableListOf("mushroom")),
        SWAMP(mutableListOf("swamp")),
        JUNGLE(mutableListOf("jungle")),
        TAIGA(mutableListOf("taiga")),
        SAVANNA(mutableListOf("savanna")),
        OCEAN(mutableListOf("ocean", "beach")),
        FLOWER(mutableListOf("flower", "meadow", "cherry")),
        ICE(mutableListOf("ice", "frozen", "snow")),
        MOUNTAIN(mutableListOf("peaks", "slopes", "stony")),
        WINDSWEPT(mutableListOf("windswept")),
        BASALT(mutableListOf("basalt_deltas")),
        CRIMSON(mutableListOf("crimson_forest")),
        WARPED(mutableListOf("warped_forest")),
        SOUL(mutableListOf("soul_sand_valley")),
        NETHER(mutableListOf("nether_wastes")),
        DEFAULT(mutableListOf(""));

        override fun toString(): String { return name[0].toString() + name.substring(1).lowercase(Locale.getDefault()) }
        fun contains(biome: ResourceKey<Biome>): Boolean { return biomes.stream().anyMatch { a: String -> biome.identifier().path.contains(a) } || this == DEFAULT }

        companion object {
            private val BIOME_CACHE = ConcurrentHashMap<ResourceKey<Biome>, Biomes>()

            fun getGroup(biomeKey: ResourceKey<Biome>): Biomes {
                return BIOME_CACHE.computeIfAbsent(biomeKey) { key ->
                    val path = key.identifier().path
                    entries.firstOrNull { group -> group != DEFAULT && group.biomes.any { keyword -> path.contains(keyword) } } ?: DEFAULT
                }
            }
        }
    }

    enum class BlockRates(val multiplier: Float, private val blocks: MutableList<Block>) {
        VERY_HIGH(
            2f, mutableListOf(
                Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ANCIENT_DEBRIS, Blocks.SCULK_SHRIEKER
            )
        ),
        HIGH(
            1.3f, mutableListOf(
                Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE
            )
        ),
        MID(
            1.1f, mutableListOf(
                Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE,
                Blocks.GOLD_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE,
                Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE
            )
        ),
        LOW(
            .85f, mutableListOf(
                Blocks.STONE, Blocks.TUFF, Blocks.ANDESITE, Blocks.GRANITE,
                Blocks.AMETHYST_BLOCK, Blocks.DRIPSTONE_BLOCK, Blocks.DIORITE, Blocks.DEEPSLATE,
                Blocks.BASALT, Blocks.BLACKSTONE, Blocks.SMOOTH_BASALT, Blocks.MAGMA_BLOCK, Blocks.END_STONE
            )
        ),
        VERY_LOW(
            .3f,
            mutableListOf(
                Blocks.CALCITE,
                Blocks.SANDSTONE,
                Blocks.NETHERRACK,
                Blocks.SOUL_SAND,
                Blocks.SOUL_SOIL,
                Blocks.GRAVEL
            )
        );

        companion object {
            private val RATE_MAP: Map<Block, Float> = IdentityHashMap<Block, Float>().apply { enumEntries<BlockRates>().forEach { entry -> entry.blocks.forEach { block -> put(block, entry.multiplier) } } }
            fun getMultiplier(state: BlockState): Float { return RATE_MAP[state.block] ?: 0f }
        }
    }
}