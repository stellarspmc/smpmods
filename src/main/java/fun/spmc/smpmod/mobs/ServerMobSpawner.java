package fun.spmc.smpmod.mobs;

import fun.spmc.smpmod.utils.ServerMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class ServerMobSpawner {
    private static final ArrayList<ServerMob> mobList = new ArrayList<>();

    public static void onEntityJoin(Entity entity, ServerLevel ignoredLevel) {
        for (ServerMob mob : mobList) {
            if (mob.getEntityType() == entity.getType() && entity.getRandom().nextFloat() >= mob.entitySpawnRate()) {
                mob.setEntity((LivingEntity) entity);
                break;
            }
        }
    }

    public static void registerMobs() {
        mobList.add(new EyeZombie());
        mobList.add(new NickZombie());
        mobList.add(new Minotaur());
    }
}
