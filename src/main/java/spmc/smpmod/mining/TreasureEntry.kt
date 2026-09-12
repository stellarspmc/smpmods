package spmc.smpmod.mining

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import spmc.smpmod.core.ItemRarity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import spmc.smpmod.core.BiomeCategory

class TreasureEntry private constructor(
		val item: Item,
		val minCount: Int,
		val maxCount: Int,
		val rarity: ItemRarity,
		val allowedBiomes: Set<BiomeCategory>,
		val modifiers: List<ItemStack.(ServerLevel) -> Unit>,
		val name: Component?,
		val lore: List<Component>
) {
	fun isValid(rarity: ItemRarity, biome: BiomeCategory): Boolean {
		if (this.rarity != rarity) return false
		if (minCount !in 1 .. maxCount) return false
		return allowedBiomes.isEmpty() || allowedBiomes.contains(biome)
	}

	fun createStack(level: ServerLevel): ItemStack {
		val count = if (minCount == maxCount) minCount else level.random.nextIntBetweenInclusive(minCount, maxCount)
		val stack = ItemStack(item, count)

		modifiers.forEach { modify -> stack.modify(level) }
		name?.let { customName -> stack.set(DataComponents.CUSTOM_NAME, customName) }
		if (lore.isNotEmpty()) stack.set(DataComponents.LORE, ItemLore(lore))

		return stack
	}

	class Builder(val item: Item) {
		var minCount: Int = 1
		var maxCount: Int = 1
		var rarity: ItemRarity = ItemRarity.COMMON
		val allowedBiomes: MutableSet<BiomeCategory> = mutableSetOf()
		val modifiers = mutableListOf<ItemStack.(ServerLevel) -> Unit>()
		var name: Component? = null
		val lore = mutableListOf<Component>()

		// Fixed count semantics: count(exact) sets both min and max to the same value
		fun count(exact: Int) = apply { minCount = exact; maxCount = exact }
		fun count(min: Int, max: Int) = apply { minCount = min; maxCount = max }

		fun rarity(rarity: ItemRarity) = apply { this.rarity = rarity }
		fun biome(vararg biomes: BiomeCategory) = apply { allowedBiomes.addAll(biomes) }
		fun biome(biomes: List<BiomeCategory>) = apply { allowedBiomes.addAll(biomes) }
		fun biome(biome: BiomeCategory) = apply { allowedBiomes.add(biome) }
		fun modify(modifier: ItemStack.(ServerLevel) -> Unit) = apply { modifiers.add(modifier) }
		fun name(name: Component) = apply { this.name = name }
		fun lore(vararg lines: Component) = apply { lore.addAll(lines) }

		fun build() = TreasureEntry(
			item = item,
			minCount = minCount,
			maxCount = maxCount,
			rarity = rarity,
			allowedBiomes = allowedBiomes.toSet(),
			modifiers = modifiers.toList(),
			name = name,
			lore = lore.toList()
		)
	}
}
