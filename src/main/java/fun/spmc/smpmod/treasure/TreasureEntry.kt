package `fun`.spmc.smpmod.treasure

import `fun`.spmc.smpmod.SMPMod.Companion.minecraftServer
import `fun`.spmc.smpmod.core.ItemRarity
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.checkerframework.checker.units.qual.m

class TreasureEntry private constructor(builder: Builder) {
    private val id: String = builder.id
    private val item: Item = builder.item
    private val minCount: Int
    private val maxCount: Int
    private val rarity: ItemRarity
    private val requiredDimension: ResourceKey<Level>?
    private val allowedBiomes: MutableSet<TreasureHelper.Biomes>

    init {
        this.minCount = builder.minCount
        this.maxCount = builder.maxCount
        this.rarity = builder.rarity
        this.requiredDimension = builder.requiredDimension
        this.allowedBiomes = builder.allowedBiomes
    }

    fun isValid(level: Level, rarity: ItemRarity, biome: TreasureHelper.Biomes): Boolean {
        if (this.rarity.ordinal != rarity.ordinal) return false
        if (requiredDimension != null && level.dimension() !== requiredDimension) return false
        if (minCount < 1 || maxCount < 1 || maxCount < minCount) return false
        return allowedBiomes.contains(biome) || allowedBiomes.isEmpty()
    }

    fun createStack(): ItemStack { return ItemStack(item, minecraftServer!!.overworld().random.nextIntBetweenInclusive(minCount, maxCount)) }
    fun getRarity(): ItemRarity { return rarity }

    class Builder(val id: String, val item: Item) {
        var minCount = 1
        var maxCount = 1
        var rarity: ItemRarity = ItemRarity.COMMON
        var requiredDimension: ResourceKey<Level>? = null
        val allowedBiomes: MutableSet<TreasureHelper.Biomes> = HashSet()

        fun count(min: Int, max: Int): Builder {
            minCount = min
            maxCount = max
            return this
        }

        fun count(max: Int): Builder {
            maxCount = max
            return this
        }

        fun rarity(rarity: ItemRarity): Builder {
            this.rarity = rarity
            return this
        }

        fun dimension(dimension: ResourceKey<Level>?): Builder {
            this.requiredDimension = dimension
            return this
        }

        fun biome(biome: TreasureHelper.Biomes): Builder {
            this.allowedBiomes.add(biome)
            return this
        }

        fun build(): TreasureEntry { return TreasureEntry(this) }
    }
}
