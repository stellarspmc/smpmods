package spmc.smpmod.registry

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import spmc.smpmod.mobs.boss.CrystalBoss

object BossRegistry {
    internal fun register() {
        PolymerRegistry.registerEntity("crystal_boss", EntityType.Builder.of(::CrystalBoss, MobCategory.MONSTER).sized(.9f, 2.9f), CrystalBoss.createAttributes())
    }
}
