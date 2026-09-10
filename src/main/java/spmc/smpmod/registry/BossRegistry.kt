package spmc.smpmod.registry

import spmc.smpmod.mobs.boss.CrystalBoss
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.Level

object BossRegistry {
    internal fun register() { // TODO: fix
        PolymerRegistry.registerEntity("crystal_boss", EntityType.Builder.of(::CrystalBoss, MobCategory.MONSTER).sized(.9f, 2.9f), CrystalBoss.createAttributes())
    }
}
