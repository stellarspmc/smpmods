package spmc.smpmod.utils

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.block.Block

abstract class BasePolymerBlockItem(block: Block, properties: Properties, private val vanillaItem: Item) : BlockItem(block, properties), PolymerItem {
	override fun getPolymerItem(itemStack: ItemStack, context: PacketContext): Item = vanillaItem
	override fun getPolymerItemModel(stack: ItemStack, context: PacketContext, lookup: HolderLookup.Provider) = BuiltInRegistries.ITEM.getKey(vanillaItem)
	override fun getName(itemStack: ItemStack): Component = buildName(itemStack)

	override fun modifyBasePolymerItemStack(out: ItemStack, stack: ItemStack, context: PacketContext, lookup: HolderLookup.Provider) {
		out.set(DataComponents.CUSTOM_NAME, buildName(stack))
		out.set(DataComponents.LORE, ItemLore(buildLore(stack)))
		modifyItem(out, stack)
	}

	abstract fun buildName(stack: ItemStack): Component
	abstract fun buildLore(stack: ItemStack): MutableList<Component>
	abstract fun modifyItem(stack: ItemStack, stackData: ItemStack)
}