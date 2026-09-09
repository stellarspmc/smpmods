package spmc.smpmod.registry

import spmc.smpmod.core.ItemRarity
import spmc.smpmod.treasure.TreasureEntry
import spmc.smpmod.treasure.TreasureHelper.Biomes
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object TreasureRegistry {
    private val REGISTRY: MutableList<TreasureEntry> = ArrayList()
    fun getEligibleTreasures(level: Level, biome: Biomes, rarity: ItemRarity): List<TreasureEntry> { return REGISTRY.stream().filter { entry: TreasureEntry -> entry.isValid(level, rarity, biome) }.toList() }

    private fun add(item: Item): TreasureEntry.Builder {
        val builder = TreasureEntry.Builder(item)
        REGISTRY.add(builder.build())
        return builder
    }

    internal fun register() {
        REGISTRY.clear()
        registerDefault()
        registerOverworld()
        registerNether()
        registerEnd()
    }

    private fun registerDefault() {
        add(Items.GLASS_BOTTLE).count(8).rarity(ItemRarity.COMMON)
        add(Items.COBWEB).count(8).rarity(ItemRarity.COMMON)
        add(Items.IRON_NUGGET).count(6).rarity(ItemRarity.COMMON)
        add(Items.COPPER_NUGGET).count(12).rarity(ItemRarity.COMMON)
        add(Items.GOLD_NUGGET).count(5).rarity(ItemRarity.COMMON)
        add(Items.TORCH).count(12).rarity(ItemRarity.COMMON)
        add(Items.STICK).count(16).rarity(ItemRarity.COMMON)
        add(Items.STRING).count(6).rarity(ItemRarity.COMMON)
        add(Items.RAIL).count(8).rarity(ItemRarity.COMMON)
        add(Items.PAPER).count(6).rarity(ItemRarity.COMMON)
        add(Items.BONE).count(4).rarity(ItemRarity.COMMON)
        add(Items.ROTTEN_FLESH).count(8).rarity(ItemRarity.COMMON)

        add(Items.EXPERIENCE_BOTTLE).count(2).rarity(ItemRarity.UNCOMMON)
        add(Items.BOOK).count(3).rarity(ItemRarity.UNCOMMON)
        add(Items.GUNPOWDER).count(4).rarity(ItemRarity.UNCOMMON)
        add(Items.SPIDER_EYE).count(3).rarity(ItemRarity.UNCOMMON)
        add(Items.SLIME_BALL).count(3).rarity(ItemRarity.UNCOMMON)
        add(Items.LEATHER).count(5).rarity(ItemRarity.UNCOMMON)
        add(Items.ARROW).count(12).rarity(ItemRarity.UNCOMMON)
    }

    private fun registerOverworld() {
        add(Items.HANGING_ROOTS).count(8).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)
        add(Items.RAW_COPPER).count(4).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)
        add(Items.RAW_IRON).count(2).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)
        add(Items.COAL).count(5).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)
        add(Items.GRAVEL).count(16).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)
        add(Items.FLINT).count(4).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)
        add(Items.OAK_PLANKS).count(16).rarity(ItemRarity.COMMON).biomes(Biomes.OVERWORLD)

        add(Items.GLOW_BERRIES).count(4).rarity(ItemRarity.COMMON).biomes(Biomes.CAVES)
        add(Items.POINTED_DRIPSTONE).count(6).rarity(ItemRarity.COMMON).biome(Biomes.DRIP)
        add(Items.MOSS_BLOCK).count(4).rarity(ItemRarity.COMMON).biome(Biomes.LUSH)
        add(Items.MANGROVE_ROOTS).count(8).rarity(ItemRarity.COMMON).biome(Biomes.SWAMP)
        add(Items.CLAY_BALL).count(8).rarity(ItemRarity.COMMON).biome(Biomes.SWAMP).biome(Biomes.OCEAN)
        add(Items.DEAD_BUSH).count(6).rarity(ItemRarity.COMMON).biome(Biomes.BADLANDS).biome(Biomes.DESERT)
        add(Items.CACTUS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.DESERT)
        add(Items.SEAGRASS).count(8).rarity(ItemRarity.COMMON).biome(Biomes.OCEAN)
        add(Items.SNOWBALL).count(16).rarity(ItemRarity.COMMON).biome(Biomes.ICE)
        add(Items.SWEET_BERRIES).count(6).rarity(ItemRarity.COMMON).biome(Biomes.TAIGA)
        add(Items.BAMBOO).count(8).rarity(ItemRarity.COMMON).biome(Biomes.JUNGLE)

        add(Items.COPPER_INGOT).count(3).rarity(ItemRarity.UNCOMMON).biomes(Biomes.OVERWORLD)
        add(Items.IRON_INGOT).count(2).rarity(ItemRarity.UNCOMMON).biomes(Biomes.OVERWORLD)
        add(Items.REDSTONE).count(8).rarity(ItemRarity.UNCOMMON).biomes(Biomes.OVERWORLD)
        add(Items.LAPIS_LAZULI).count(8).rarity(ItemRarity.UNCOMMON).biomes(Biomes.OVERWORLD)
        add(Items.CONCRETE.white).count(8).rarity(ItemRarity.UNCOMMON).biomes(Biomes.OVERWORLD)

        add(Items.AMETHYST_SHARD).count(4).rarity(ItemRarity.UNCOMMON).biomes(Biomes.CAVES)
        add(Items.TERRACOTTA).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.BADLANDS)
        add(Items.PACKED_ICE).count(6).rarity(ItemRarity.UNCOMMON).biome(Biomes.ICE)
        add(Items.PRISMARINE_SHARD).count(4).rarity(ItemRarity.UNCOMMON).biome(Biomes.OCEAN)
        add(Items.HONEYCOMB).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.FLOWER)
        add(Items.COCOA_BEANS).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.JUNGLE)
    }

    private fun registerNether() {
        add(Items.GLOWSTONE_DUST).count(12).rarity(ItemRarity.COMMON).biomes(Biomes.NETHER_LIST)
        add(Items.NETHERRACK).count(32).rarity(ItemRarity.COMMON).biomes(Biomes.NETHER_LIST)
        add(Items.NETHER_BRICK).count(8).rarity(ItemRarity.COMMON).biomes(Biomes.NETHER_LIST)

        add(Items.SOUL_SAND).count(12).rarity(ItemRarity.COMMON).biome(Biomes.SOUL)
        add(Items.SOUL_SOIL).count(12).rarity(ItemRarity.COMMON).biome(Biomes.SOUL)
        add(Items.BASALT).count(16).rarity(ItemRarity.COMMON).biome(Biomes.BASALT)
        add(Items.BLACKSTONE).count(16).rarity(ItemRarity.COMMON).biome(Biomes.BASALT)
        add(Items.MAGMA_CREAM).count(2).rarity(ItemRarity.COMMON).biome(Biomes.BASALT)
        add(Items.CRIMSON_ROOTS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.CRIMSON)
        add(Items.WARPED_ROOTS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.WARPED)
        add(Items.NETHER_SPROUTS).count(6).rarity(ItemRarity.COMMON).biome(Biomes.WARPED)

        add(Items.QUARTZ).count(8).rarity(ItemRarity.UNCOMMON).biomes(Biomes.NETHER_LIST)
        add(Items.GOLD_INGOT).count(2).rarity(ItemRarity.UNCOMMON).biomes(Biomes.NETHER_LIST)
        add(Items.GLOWSTONE).count(2).rarity(ItemRarity.UNCOMMON).biomes(Biomes.NETHER_LIST)

        add(Items.GHAST_TEAR).count(1).rarity(ItemRarity.UNCOMMON).biome(Biomes.SOUL)
        add(Items.BLAZE_POWDER).count(2).rarity(ItemRarity.UNCOMMON).biomes(Biomes.NETHER_LIST)
        add(Items.CRIMSON_STEM).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.CRIMSON)
        add(Items.WARPED_STEM).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.WARPED)
        add(Items.CRIMSON_FUNGUS).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.CRIMSON)
        add(Items.WARPED_FUNGUS).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.WARPED)
    }

    private fun registerEnd() {
        add(Items.END_STONE).count(32).rarity(ItemRarity.COMMON).biomes(Biomes.END_LIST)

        add(Items.CHORUS_FRUIT).count(6).rarity(ItemRarity.UNCOMMON).biomes(Biomes.END_LIST)
        add(Items.POPPED_CHORUS_FRUIT).count(4).rarity(ItemRarity.UNCOMMON).biomes(Biomes.END_LIST)
    }
}
