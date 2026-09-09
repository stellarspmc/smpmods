package `fun`.spmc.smpmod.registry

import `fun`.spmc.smpmod.mobs.boss.CrystalBoss
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.Level

object BossRegistry {
    @JvmStatic
    fun register() {
        PolymerRegistry.registerEntity<CrystalBoss?>(
            "crystal_boss",
            EntityType.Builder.of({ entityType: EntityType<CrystalBoss>, level: Level -> CrystalBoss(entityType, level) }, MobCategory.MONSTER).sized(0.9f, 2.9f),
            CrystalBoss.createAttributes()
        )
    }
}
