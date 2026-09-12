package spmc.smpmod.fishing

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes

object FishMob {
	val mobsToSpawn: List<EntityType<out Entity>> = listOf(EntityTypes.GUARDIAN, EntityTypes.ELDER_GUARDIAN, EntityTypes.PHANTOM) // TODO: add more mobs
	val healthRange: IntRange = 15..1250

	fun spawnMob(id: String, pos: BlockPos) {

	}

}