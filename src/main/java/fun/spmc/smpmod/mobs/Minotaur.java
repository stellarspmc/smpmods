package fun.spmc.smpmod.mobs;

import fun.spmc.smpmod.utils.ServerMob;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Objects;

public class Minotaur implements ServerMob {
    @Override public EntityType<? extends LivingEntity> getEntityType() { return EntityTypes.RAVAGER; }
    @Override public double entitySpawnRate() { return .5; }
    @Override public Component getEntityName() { return Component.literal("Minotaur"); }

    @Override public void setHead(LivingEntity entity) {}
    @Override public void setChest(LivingEntity entity) {}
    @Override public void setLegs(LivingEntity entity) {}
    @Override public void setBoots(LivingEntity entity) {}

    @Override public void setItems(LivingEntity entity) {}
    @Override public void setEffects(LivingEntity entity) {}

    @Override
    public void setAttributes(LivingEntity entity) {
        Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(600);
        entity.setHealth(600);

        Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(2*Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)).getBaseValue());
    }
}
