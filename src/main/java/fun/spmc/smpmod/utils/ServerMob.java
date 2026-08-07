package fun.spmc.smpmod.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public interface ServerMob {
    EntityType<? extends LivingEntity> getEntityType();
    double entitySpawnRate();
    Component getEntityName();

    void setHead(LivingEntity entity);
    void setChest(LivingEntity entity);
    void setLegs(LivingEntity entity);
    void setBoots(LivingEntity entity);

    void setItems(LivingEntity entity);

    void setEffects(LivingEntity entity);
    void setAttributes(LivingEntity entity);

    default void setEntity(LivingEntity entity) {
        entity.setCustomName(getEntityName());

        setHead(entity);
        setChest(entity);
        setLegs(entity);
        setBoots(entity);

        setItems(entity);

        setEffects(entity);
        setAttributes(entity);
    }
}
