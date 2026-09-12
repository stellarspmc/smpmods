package spmc.smpmod.plant

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.utils.BasePolymerItem
import spmc.smpmod.utils.UtilFunc.rnd2DP
import java.util.*
import kotlin.math.roundToInt

class CropItem(settings: Properties, vanillaItem: Item, val cropName: String, val basePrice: Double, val rarity: ItemRarity): BasePolymerItem(settings, vanillaItem) {
	override fun buildName(stack: ItemStack): Component {
		val quality: Int = getQuality(getCropTag(stack))
		val traits: MutableSet<ItemModifier> = getModifiers(getCropTag(stack)).keys
		val title = Component.empty()
		for (trait in traits) title.append(Component.literal("$trait ").withColor(trait.color))
		title.append(Component.literal(this.cropName).withColor(rarity.color))
		if (quality != 0) title.append(Component.literal(" " + (if (quality > 0) "★".repeat(quality) else "\uD83D\uDC80".repeat(-quality))).withStyle(if (quality > 0) ChatFormatting.YELLOW else ChatFormatting.DARK_RED))
		return title.withStyle { it.withItalic(false) }
	}

	override fun buildLore(stack: ItemStack): MutableList<Component> {
		val lore: MutableList<Component> = ArrayList()
		lore.add(Component.literal("Value: $" + getModifiedPrice(stack)).withStyle(ChatFormatting.GOLD).withStyle { it.withItalic(false) })
		return lore
	}

	override fun modifyItem(stack: ItemStack, stackData: ItemStack) {
		val tag: CompoundTag = getCropTag(stackData)
		val quality: Int = getQuality(tag)
		if (quality !in 1 ..< 4) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
	}

	fun createCropInstance(quality: Int, mods: MutableMap<ItemModifier, Int>): ItemStack {
		val stack = ItemStack(this)
		val cropTag = CompoundTag()
		cropTag.putString("id", cropName.lowercase(Locale.getDefault()).replace(" ", "_"))
		cropTag.putInt("quality", quality)

		val modTag = CompoundTag()
		mods.forEach { (mod, level) -> modTag.putInt(mod.name.lowercase(Locale.getDefault()), level) }
		cropTag.put("modifier", modTag)

		CustomData.update(DataComponents.CUSTOM_DATA, stack) { it.put("crop", cropTag) }
		return stack
	}

	fun getModifiedPrice(stack: ItemStack): Double {
		val tag: CompoundTag = getCropTag(stack)
		val quality: Int = getQuality(tag)
		val price = this.basePrice * stack.count
		return rnd2DP(price * (1 + quality * .45))
	}

	companion object {
		fun getCropTag(stack: ItemStack) = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("crop").orElse(CompoundTag())!!
		fun getQuality(cropTag: CompoundTag) = cropTag.getIntOr("quality", 1)

		private fun getModifiers(tag: CompoundTag): MutableMap<ItemModifier, Int> {
			val map: MutableMap<ItemModifier, Int> = mutableMapOf()
			if (tag.getCompound("modifier").isPresent) tag.getCompound("modifier").get().forEach { id, level -> ItemModifier.fromId(id).ifPresent { mod -> level.asInt().ifPresent { map[mod] = it } } }
			return map
		}
	}
}
