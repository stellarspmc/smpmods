package `fun`.spmc.smpmod.registry

import `fun`.spmc.smpmod.core.ItemRarity
import `fun`.spmc.smpmod.treasure.TreasureEntry
import `fun`.spmc.smpmod.treasure.TreasureHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object TreasureRegistry {
    private val REGISTRY: MutableList<TreasureEntry> = ArrayList()

    fun register() {
        REGISTRY.clear()

        add("raw_iron_cluster", Items.RAW_IRON).count(3).rarity(ItemRarity.COMMON)
        add("raw_gold_cluster", Items.RAW_GOLD).count(2).rarity(ItemRarity.UNCOMMON)
        add("badlands_gold", Items.GOLD_BLOCK).rarity(ItemRarity.RARE).biome(TreasureHelper.Biomes.BADLANDS)
        add("ancient_debris_chunk", Items.ANCIENT_DEBRIS).rarity(ItemRarity.MYTHIC)
        add("echo_core", Items.ECHO_SHARD).count(2).rarity(ItemRarity.LEGENDARY).biome(TreasureHelper.Biomes.SCULK)
        add("nether_star_fragment", Items.NETHER_STAR).rarity(ItemRarity.ASTRAL).dimension(Level.NETHER)
    }

    fun getEligibleTreasures(level: Level, biome: TreasureHelper.Biomes, rarity: ItemRarity): List<TreasureEntry> { return REGISTRY.stream().filter { entry: TreasureEntry -> entry.isValid(level, rarity, biome) }.toList() }

    private fun add(id: String, item: Item): TreasureEntry.Builder {
        val builder = TreasureEntry.Builder(id, item)
        REGISTRY.add(builder.build())
        return builder
    }
}
