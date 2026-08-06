package fun.spmc.smpmod.events.mobs;

import fun.spmc.smpmod.events.ServerMob;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class CrystalMaster implements ServerMob {
    @Override
    public EntityType<? extends LivingEntity> getEntityType() {
        return null;
    }

    @Override
    public double entitySpawnRate() {
        return 0;
    }

    @Override
    public Component getEntityName() {
        return null;
    }

    @Override
    public void setHead(LivingEntity entity) {

    }

    @Override
    public void setChest(LivingEntity entity) {

    }

    @Override
    public void setLegs(LivingEntity entity) {

    }

    @Override
    public void setBoots(LivingEntity entity) {

    }

    @Override
    public void setItems(LivingEntity entity) {

    }

    @Override
    public void setEffects(LivingEntity entity) {

    }

    @Override
    public void setAttributes(LivingEntity entity) {

    }
}
