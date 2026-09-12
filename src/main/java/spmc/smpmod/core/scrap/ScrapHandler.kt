package spmc.smpmod.core.scrap

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.golem.CopperGolem
import net.minecraft.world.entity.animal.golem.IronGolem
import net.minecraft.world.entity.animal.golem.SnowGolem
import net.minecraft.world.entity.monster.cubemob.SulfurCube

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
