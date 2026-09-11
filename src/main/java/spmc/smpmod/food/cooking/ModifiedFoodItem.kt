package spmc.smpmod.food.cooking

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import spmc.smpmod.utils.BasePolymerItem

class ModifiedFoodItem(properties: Properties, vanillaItem: Item): BasePolymerItem(properties, vanillaItem) {
	override fun buildName(stack: ItemStack): Component {
		TODO("Not yet implemented")
	}

	override fun buildLore(stack: ItemStack): MutableList<Component> {
		TODO("Not yet implemented")
	}

	override fun modifyItem(stack: ItemStack, stackData: ItemStack) {
		TODO("Not yet implemented")
	}
}