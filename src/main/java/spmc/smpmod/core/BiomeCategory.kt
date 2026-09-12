package spmc.smpmod.core

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
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
	DESERT(listOf(ConventionalBiomeTags.IS_DRY_OVERWORLD, ConventionalBiomeTags.IS_DESERT)),
	SNOWY(listOf(ConventionalBiomeTags.IS_SNOWY, ConventionalBiomeTags.IS_COLD_OVERWORLD)),
	TROPICAL(listOf(ConventionalBiomeTags.IS_JUNGLE, ConventionalBiomeTags.IS_SAVANNA, ConventionalBiomeTags.IS_SWAMP, createBiomeTag("is_tropical"), BiomeTags.IS_JUNGLE, BiomeTags.IS_SAVANNA)), // TODO: remove tropical tag with ConventionalBiomeTags
	OCEAN(listOf(ConventionalBiomeTags.IS_OCEAN, ConventionalBiomeTags.IS_RIVER, BiomeTags.IS_OCEAN, BiomeTags.IS_RIVER, BiomeTags.IS_BEACH)),
	FOREST(listOf(ConventionalBiomeTags.IS_FOREST)), // TODO
	TAIGA(listOf(BiomeTags.IS_TAIGA, ConventionalBiomeTags.IS_TAIGA)),
	PLAINS(listOf(BiomeTags.IS_HILL, ConventionalBiomeTags.IS_HILL)), // TODO: check
	FLOWER(listOf(ConventionalBiomeTags.IS_FLORAL, ConventionalBiomeTags.IS_FLOWER_FOREST)), // TODO
	MOUNTAIN(listOf(BiomeTags.IS_MOUNTAIN)), // TODO: checks "peaks", "slopes", "stony", "windswept"
	MUSHROOM(listOf(BiomeTags.WITHOUT_ZOMBIE_SIEGES)),

	CAVE(listOf(ConventionalBiomeTags.IS_CAVE)),
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

	override fun toString() = name[0].toString() + name.substring(1).lowercase(Locale.getDefault())
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