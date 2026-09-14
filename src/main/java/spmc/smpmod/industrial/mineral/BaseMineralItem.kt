package spmc.smpmod.industrial.mineral

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import spmc.smpmod.core.*
import spmc.smpmod.utils.BasePolymerItem
import java.util.*

class BaseMineralItem: BasePolymerItem {
	private val headTexture: String?
	private val id: Component

	constructor(properties: Properties, vanillaItem: Item, id: Component): super(properties, vanillaItem) {
		this.headTexture = null
		this.id = id
	}

	// heads
	constructor(properties: Properties, headTexture: String?, id: Component): super(properties, Items.PLAYER_HEAD) {
		this.headTexture = headTexture
		this.id = id
	}

	override fun buildName(stack: ItemStack) = Component.empty().append(id).withStyle { it.withItalic(false) }
	override fun buildLore(stack: ItemStack) = mutableListOf<Component>()
	override fun modifyItem(stack: ItemStack, stackData: ItemStack) { if (headTexture != null) stack.set(DataComponents.PROFILE, createCustomProfile("PolymerItem", UUID.randomUUID(), headTexture)) }
}
