package fun.spmc.smpmod.events;

import fun.spmc.smpmod.mobs.EyeZombie;
import fun.spmc.smpmod.mobs.Minotaur;
import fun.spmc.smpmod.mobs.NickZombie;
import fun.spmc.smpmod.utils.ServerMob;
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
        mobList.add(new EyeZombie());
        mobList.add(new NickZombie());
        mobList.add(new Minotaur());
    }
}
