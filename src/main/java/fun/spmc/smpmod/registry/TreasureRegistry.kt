package `fun`.spmc.smpmod.registry

import `fun`.spmc.smpmod.core.ItemRarity
import `fun`.spmc.smpmod.treasure.TreasureEntry
import `fun`.spmc.smpmod.treasure.TreasureHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object TreasureRegistry {
    private val REGISTRY: MutableList<TreasureEntry> = ArrayList()
    fun getEligibleTreasures(level: Level, biome: TreasureHelper.Biomes, rarity: ItemRarity): List<TreasureEntry> { return REGISTRY.stream().filter { entry: TreasureEntry -> entry.isValid(level, rarity, biome) }.toList() }

    private fun add(item: Item): TreasureEntry.Builder {
        val builder = TreasureEntry.Builder(item)
        REGISTRY.add(builder.build())
        return builder
    }

    internal fun register() {
        REGISTRY.clear()

    }

    private fun registerDefault() {
        add(Items.GLASS_BOTTLE).count(8).rarity(ItemRarity.COMMON)
        add(Items.DEAD_BUSH).count(6).rarity(ItemRarity.COMMON)
        add(Items.COBWEB).count(12).rarity(ItemRarity.COMMON)
        add(Items.HANGING_ROOTS).count(4).rarity(ItemRarity.COMMON)
        add(Items.GLASS_BOTTLE).count(8).rarity(ItemRarity.COMMON)
        add(Items.GLASS_BOTTLE).count(8).rarity(ItemRarity.COMMON)
        add(Items.GLASS_BOTTLE).count(8).rarity(ItemRarity.COMMON)
    }

    private fun registerNether() {

    }

    private fun registerOverworld() {

    }
}
