package fun.spmc.smpmod.events;

import fun.spmc.smpmod.events.mobs.EyeBoss;
import fun.spmc.smpmod.events.mobs.NickBoss;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class ServerMobSpawner {
    private static final ArrayList<ServerMob> mobList = new ArrayList<>();

    public static void onEntityJoin(Entity entity, ServerLevel ignoredLevel) {
        for (ServerMob mob : mobList.stream().filter(mob -> mob.getEntityType() == entity.getType()).toList()) {
            if (entity.getRandom().nextFloat() >= mob.entitySpawnRate()) {
                mob.setEntity((LivingEntity) entity);
                break;
            }
        }
    }

    public static void registerMobs() {
        mobList.add(new EyeBoss());
        mobList.add(new NickBoss());
    }
}
