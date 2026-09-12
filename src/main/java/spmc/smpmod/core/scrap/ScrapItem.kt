package spmc.smpmod.core.scrap

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.utils.BasePolymerItem

class ScrapItem(properties: Properties, vanillaItem: Item, val rarity: ItemRarity): BasePolymerItem(properties, vanillaItem) {
	private val mods: MutableList<ItemModifier> = mutableListOf()

	override fun buildName(stack: ItemStack): Component = Component.literal("${rarity.name} Scrap").withColor(rarity.color)
	fun addModifiers(mod: ItemModifier) = apply { mods.add(mod) }

	override fun buildLore(stack: ItemStack): MutableList<Component> { return mutableListOf() } // override what could be done with scraps
	override fun modifyItem(stack: ItemStack, stackData: ItemStack) { if (rarity.shouldAnnounce()) applyGlint(stack, stackData) }


	/*private fun applicableItems(): ArmorSlot {
		return ArmorSlot()
	}*/
}
