package spmc.smpmod.core

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.golem.CopperGolem
import net.minecraft.world.entity.animal.golem.IronGolem
import net.minecraft.world.entity.animal.golem.SnowGolem
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.ElderGuardian
import net.minecraft.world.entity.monster.cubemob.SulfurCube
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import spmc.smpmod.registry.PolymerRegistry
import spmc.smpmod.utils.BasePolymerItem
import spmc.smpmod.utils.formatName

class ScrapItem(properties: Properties, vanillaItem: Item, val rarity: ItemRarity): BasePolymerItem(properties, vanillaItem) {
	private val mods: MutableList<ItemModifier> = mutableListOf()

	override fun buildName(stack: ItemStack): Component = Component.literal(getName(rarity)).withColor(rarity.color)
	fun addModifiers(mod: ItemModifier) = apply { mods.add(mod) } // applied to the item TODO

	override fun buildLore(stack: ItemStack): MutableList<Component> { return mutableListOf() } // override what could be done with scraps
	override fun modifyItem(stack: ItemStack, stackData: ItemStack) { if (rarity.shouldAnnounce()) applyGlint(stack, stackData) }

	companion object {
		private fun getName(rarity: ItemRarity) = when(rarity) {
			ItemRarity.RARE -> "${formatName(rarity.name)} Relic"
			ItemRarity.EPIC -> "${formatName(rarity.name)} Catalyst"
			ItemRarity.LEGENDARY -> "${formatName(rarity.name)} Matrix"
			ItemRarity.MYTHIC -> "${formatName(rarity.name)} Singularity"
			ItemRarity.CHROMATIC -> "${formatName(rarity.name)} Glint"
			ItemRarity.ASTRAL -> "${formatName(rarity.name)} Fabric"
			else -> "${formatName(rarity.name)} Scrap"
		}
	}
}

object ScrapHandler {
	var buffMultiplier = 1f
	// TODO: broken (only 50th line is broken)
	fun handleMonsterScrap(entity: Entity) {
		if (entity is SulfurCube) return
		val random = entity.random
		if (random.nextFloat() < (0.1f * buffMultiplier)) getScrap(rollScrapRarity(random))?.let { entity.spawnAtLocation(entity.level() as? ServerLevel ?: return@let, ItemStack(it, random.nextInt(1, 3))) }
		if (entity is WitherBoss || entity is EnderDragon || entity is ElderGuardian || entity is Warden) getScrap(rollGolemScrapRarity(random))?.let { entity.spawnAtLocation(entity.level() as? ServerLevel ?: return@let, ItemStack(it, random.nextInt(1, 6))) }
	}

	fun handleGolemScrap(entity: Entity) {
		if (entity !is IronGolem && entity !is CopperGolem && entity !is SnowGolem) return
		val random = entity.random
		if (random.nextFloat() < (0.25f * buffMultiplier) && entity is IronGolem) getScrap(rollGolemScrapRarity(random))?.let { entity.spawnAtLocation(entity.level() as? ServerLevel ?: return@let, ItemStack(it, random.nextInt(1, 4))) }
		if (random.nextFloat() < (0.4f * buffMultiplier) && entity is CopperGolem || entity is SnowGolem) getScrap(rollScrapRarity(random))?.let { entity.spawnAtLocation(entity.level() as? ServerLevel ?: return@let, ItemStack(it, random.nextInt(1, 3))) }
	}

	private fun rollScrapRarity(random: RandomSource): ItemRarity {
		val roll = random.nextDouble()
		return when {
			roll < .01 -> rollGolemScrapRarity(random)
			roll < .03 -> ItemRarity.LEGENDARY
			roll < .07 -> ItemRarity.EPIC
			roll < .2 -> ItemRarity.RARE
			roll < .5 -> ItemRarity.UNCOMMON
			else -> ItemRarity.COMMON
		}
	}

	private fun rollGolemScrapRarity(random: RandomSource): ItemRarity {
		val roll = random.nextDouble()
		return when {
			roll < .005 -> ItemRarity.ASTRAL
			roll < .02 -> ItemRarity.CHROMATIC
			roll < .08 -> ItemRarity.MYTHIC
			roll < .25 -> ItemRarity.LEGENDARY
			else -> ItemRarity.RARE
		}
	}

	fun getScrap(rarity: ItemRarity): ScrapItem? {
		return when (rarity) {
			ItemRarity.COMMON -> PolymerRegistry.getItem("common_scrap")?.value() as? ScrapItem
			ItemRarity.UNCOMMON -> PolymerRegistry.getItem("uncommon_scrap")?.value() as? ScrapItem
			ItemRarity.RARE -> PolymerRegistry.getItem("rare_scrap")?.value() as? ScrapItem
			ItemRarity.EPIC -> PolymerRegistry.getItem("epic_scrap")?.value() as? ScrapItem
			ItemRarity.LEGENDARY -> PolymerRegistry.getItem("legendary_scrap")?.value() as? ScrapItem
			ItemRarity.MYTHIC -> PolymerRegistry.getItem("mythic_scrap")?.value() as? ScrapItem
			ItemRarity.CHROMATIC -> PolymerRegistry.getItem("chromatic_glint")?.value() as? ScrapItem
			ItemRarity.ASTRAL -> PolymerRegistry.getItem("astral_fabric")?.value() as? ScrapItem
		}
	}
}
