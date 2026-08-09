package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.mobs.boss.CrystalBoss;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class PolymerMisc {
    protected static void register() {
        PolymerRegistry.registerEntity("crystal_boss", EntityType.Builder.of(CrystalBoss::new, MobCategory.MONSTER).sized(0.9f, 2.9f), CrystalBoss.createAttributes().build());
    }
}
