package spmc.smpmod.mining

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import spmc.smpmod.utils.BasePolymerItem
// val name: String, val color: TextColor, vararg val mods: ItemModifier, repaired: Item, val stats: RodStats,
//              @JvmField val tier: TierSystem
class TreasurePickaxeItem(properties: Properties, private val vanillaItem: Item, /*private val tier: PickaxeTiers,*/ private val durability: Int): BasePolymerItem(properties.stacksTo(1).durability(durability), vanillaItem) {
	override fun buildName(stack: ItemStack): Component { TODO("Not yet implemented") }
	override fun buildLore(stack: ItemStack): MutableList<Component> { TODO("Not yet implemented") }

	override fun modifyItem(stack: ItemStack, stackData: ItemStack) {
		TODO("Not yet implemented")
	}
}
