package spmc.smpmod.fishing

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.utils.BasePolymerItem
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

class FishItem(settings: Properties, vanillaItem: Item, @JvmField val fishName: String, @JvmField val basePrice: Double, @JvmField val rarity: ItemRarity): BasePolymerItem(settings.stacksTo(64), vanillaItem) {
	override fun modifyItem(stack: ItemStack, stackData: ItemStack) {
		val fishTag = stackData.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish")
		if (fishTag.isPresent) {
			val quality: Int = getQuality(fishTag.get())
			if (quality > 3) {
				stackData.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
				stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
			}
		}
	}

	override fun buildName(stack: ItemStack): Component {
		val fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish")
		if (fishTag.isPresent) {
			val tag = fishTag.get()
			val traits: MutableSet<ItemModifier> = getModifiers(tag).keys
			val quality: Int = getQuality(tag)

			val title = Component.empty()
			for (trait in traits) title.append(Component.literal("$trait ").withColor(trait.color))
			title.append(Component.literal(this.fishName).withColor(rarity.color))
			if (quality > 0) title.append(Component.literal(" " + "★".repeat(quality)).withStyle(ChatFormatting.YELLOW))
			return title.withStyle { it.withItalic(false) }
		}
		return Component.literal(this.fishName).withColor(rarity.color).withStyle { it.withItalic(false) }
	}

	override fun buildLore(stack: ItemStack): MutableList<Component> {
		val lore = mutableListOf<Component>()
		val fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish")
		if (fishTag.isPresent) lore.add(Component.literal("Price: ").withStyle(ChatFormatting.GRAY).append(Component.literal("$" + getModifiedPrice(stack)).withStyle(ChatFormatting.GREEN)).withStyle { it.withItalic(false) })
		return lore
	}

	fun createFishInstance(quality: Int, mods: MutableMap<ItemModifier, Int>): ItemStack {
		val stack = ItemStack(this)

		val fishData = CompoundTag()
		fishData.putString("id", fishName.lowercase(Locale.getDefault()).replace(" ", "_"))
		fishData.putInt("quality", quality)

		val modTag = CompoundTag()
		mods.forEach { (mod, level) -> modTag.putInt(mod.name.lowercase(Locale.getDefault()), level) }
		fishData.put("modifier", modTag)
		CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag -> tag.put("fish", fishData) }
		return stack
	}

	companion object {
		private fun getModifiers(tag: CompoundTag): MutableMap<ItemModifier, Int> {
			val map = mutableMapOf<ItemModifier, Int>()
			if (tag.getCompound("modifier").isPresent) {
				val modTag = tag.getCompound("modifier").get()
				modTag.forEach { id, level -> ItemModifier.fromId(id).ifPresent { mod -> level.asInt().ifPresent { lvl -> map[mod] = lvl }} }
			}
			return map
		}

		private fun getQuality(tag: CompoundTag) = tag.getIntOr("quality", 0)
		fun getModifiedPrice(stack: ItemStack): Double {
			val fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish")
			if (fishTag.isPresent) {
				val tag = fishTag.get()
				val quality: Int = getQuality(tag)
				val modifiers = getModifiers(tag)
				var price = ((stack.item) as FishItem).basePrice * stack.count
				for ((key, value) in modifiers) price *= key.priceMultiplier * min(1, value)
				return (price * (quality * .15 + 1) * 100).roundToInt() / 100.0
			}
			return ((stack.item) as FishItem).basePrice * stack.count
		}
	}
}