package spmc.smpmod.core

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.golem.CopperGolem
import net.minecraft.world.entity.animal.golem.IronGolem
import net.minecraft.world.entity.animal.golem.SnowGolem
import net.minecraft.world.entity.monster.cubemob.SulfurCube
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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

object ScrapHandler {
	fun handleMonsterScrap(entity: Entity) {
		if (entity is SulfurCube) return
	}

	fun handleGolemScrap(entity: Entity) {
		if (entity !is IronGolem && entity !is CopperGolem && entity !is SnowGolem) return

	}

	// scraps are defined using ItemRarity again...
	// can be modified? TODO: think about the possibility
}
