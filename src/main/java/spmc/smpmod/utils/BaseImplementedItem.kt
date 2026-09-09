package spmc.smpmod.utils

import spmc.smpmod.utils.MessageUtils.formatName
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

class BaseImplementedItem(block: Block, properties: Properties, vanillaItem: Item, private val id: String) : BasePolymerBlockItem(block, properties, vanillaItem) {

    override fun buildName(stack: ItemStack): Component { return Component.literal(formatName(id)).withStyle { style: Style -> style.withItalic(false) }}
    override fun buildLore(stack: ItemStack): MutableList<Component> { return mutableListOf() }

    override fun modifyItem(stack: ItemStack, stackData: ItemStack) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        stackData.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    }
}
