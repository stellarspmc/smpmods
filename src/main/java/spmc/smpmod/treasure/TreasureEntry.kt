package spmc.smpmod.treasure

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import spmc.smpmod.SMPMod.Companion.minecraftServer
import spmc.smpmod.core.ItemRarity
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

class TreasureEntry private constructor(builder: Builder) {
    private val item = builder.item
    private val minCount: Int
    private val maxCount: Int
    private val rarity: ItemRarity
    private val allowedBiomes: MutableSet<TreasureHelper.Biomes>
	private val modifiers: List<ItemStack.(ServerLevel) -> Unit>
	private val name: Component?
	private val lore: List<Component>

    init {
        this.minCount = builder.minCount
        this.maxCount = builder.maxCount
        this.rarity = builder.rarity
        this.allowedBiomes = builder.allowedBiomes
	    this.modifiers = builder.modifiers
	    this.name = builder.name
	    this.lore = builder.lore
    }

    fun isValid(rarity: ItemRarity, biome: TreasureHelper.Biomes): Boolean {
        if (this.rarity.ordinal != rarity.ordinal) return false
        if (minCount < 1 || maxCount < 1 || maxCount < minCount) return false
        return allowedBiomes.contains(biome) || allowedBiomes.isEmpty()
    }

	fun getRarity() = rarity
    fun createStack(level: ServerLevel): ItemStack {
		val stack = ItemStack(item, (minecraftServer?: return ItemStack(item, (minCount + maxCount) / 2)).overworld().random.nextIntBetweenInclusive(minCount, maxCount))
	    modifiers.forEach { modify -> stack.modify(level) }
	    name?.let { customName -> stack.set(DataComponents.CUSTOM_NAME, customName) }
	    if (lore.isNotEmpty()) stack.set(DataComponents.LORE, ItemLore(lore))
		return stack
	}

    class Builder(val item: Item) {
	    val modifiers = mutableListOf<ItemStack.(ServerLevel) -> Unit>()
        var minCount = 1
        var maxCount = 1
        var rarity: ItemRarity = ItemRarity.COMMON
        val allowedBiomes: MutableSet<TreasureHelper.Biomes> = HashSet()
	    var name: Component? = null
	    var lore: MutableList<Component> = mutableListOf()

        fun count(min: Int, max: Int) = apply { minCount = min; maxCount = max }
        fun count(max: Int) = apply { maxCount = max }
        fun rarity(rarity: ItemRarity) = apply { this.rarity = rarity }
        fun biome(biome: TreasureHelper.Biomes) = apply { this.allowedBiomes.add(biome) }
        fun biome(biomes: List<TreasureHelper.Biomes>) = apply { this.allowedBiomes.addAll(biomes) }
	    fun modify(modifier: ItemStack.(ServerLevel) -> Unit) = apply { this.modifiers.add(modifier) }
	    fun name(name: Component) = apply { this.name = name }
	    fun lore(line: Component) = apply { this.lore.add(line) }
	    fun lore(vararg lines: Component) = apply { this.lore.addAll(lines) }

        fun build() = TreasureEntry(this)
    }
}
