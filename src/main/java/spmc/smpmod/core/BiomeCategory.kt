package spmc.smpmod.core

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BiomeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import spmc.smpmod.utils.UtilFunc.createBiomeTag
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class BiomeCategory(val tags: List<TagKey<Biome>> = emptyList()) {
	BADLANDS(listOf(BiomeTags.IS_BADLANDS)),
	DESERT(listOf(createBiomeTag("is_dry"), createBiomeTag("is_desert"))),
	SNOWY(listOf(createBiomeTag("is_snowy"), createBiomeTag("is_cold"))),
	TROPICAL(listOf(createBiomeTag("is_jungle"), createBiomeTag("is_savanna"), createBiomeTag("is_swamp"), createBiomeTag("is_tropical"), BiomeTags.IS_JUNGLE, BiomeTags.IS_SAVANNA)),
	OCEAN(listOf(createBiomeTag("is_ocean"), createBiomeTag("is_river"), BiomeTags.IS_OCEAN, BiomeTags.IS_RIVER, BiomeTags.IS_BEACH)),
	FOREST(listOf(createBiomeTag("is_forest"))), // TODO
	TAIGA(listOf(BiomeTags.IS_TAIGA)),
	PLAINS(listOf(BiomeTags.IS_HILL)), // TODO: check
	FLOWER, // TODO
	MOUNTAIN(listOf(BiomeTags.IS_MOUNTAIN)), // TODO: checks "peaks", "slopes", "stony", "windswept"
	MUSHROOM(listOf(BiomeTags.WITHOUT_ZOMBIE_SIEGES)),

	CAVE(listOf(createBiomeTag("is_cave"))),
	SCULK(listOf(BiomeTags.HAS_ANCIENT_CITY)), // TODO

	DEEP,
	SKY,

	NETHER(listOf(BiomeTags.IS_NETHER)), // general nether
	CRIMSON,
	WARPED,
	SOUL_SAND,
	BASALT,
	NETHER_WASTES,

	END(listOf(BiomeTags.IS_END)), // probably includes every end biome? TODO: add nullscape diversity (not a good idea to put here)
	// END_ISLANDS? Identifier.fromNamespaceAndPath("c", "end_islands"))
	DEFAULT;

	override fun toString() = name.lowercase().replaceFirstChar { it.uppercase() }
	companion object {
		val OVERWORLD = listOf(BADLANDS, DESERT, CAVE, FOREST, SCULK, MUSHROOM, TROPICAL, TAIGA, OCEAN, FLOWER, MOUNTAIN, SNOWY, SKY)

		private val BIOME_CACHE = ConcurrentHashMap<ResourceKey<Biome>, BiomeCategory>()

		fun getGroup(biome: Holder<Biome>): BiomeCategory = BIOME_CACHE.computeIfAbsent(biome.unwrapKey().orElse(null) ?: return DEFAULT) { BiomeCategory.entries.firstOrNull { group -> group != DEFAULT && (group.tags.any { biome.`is`(it) }) } ?: DEFAULT } // TODO: EITHER remove edge cases / change how it works (NOPE, adding own biome tags)
		fun getPlayerCategories(player: Player): Set<BiomeCategory> {
			val level = player.level()
			val yPos = player.y

			if (level.dimension() == Level.NETHER) {
				val biome = getGroup(level.getBiome(player.blockPosition()))
				return if (biome != DEFAULT) EnumSet.of(biome) else EnumSet.of(NETHER_WASTES)
			}
			if (level.dimension() == Level.END) return EnumSet.of(END)

			if (yPos > 240) return EnumSet.of(SKY)
			if (yPos < 0) return EnumSet.of(DEEP) // TODO: merge sculk with deep? / vice versa? (only for fishing)

			val biomeHolder = level.getBiome(player.blockPosition())
			val category = getGroup(biomeHolder)
			return if (category == DEFAULT) EnumSet.of(DEFAULT) else EnumSet.of(category)
		}
	}
}