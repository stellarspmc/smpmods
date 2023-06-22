package ml.spmc.smpmod.minecraft.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public class MobSpawnedEvent {
    public static void onEntityJoin(ServerWorld level, Entity entity) {
        mushroom(entity);
    }

    private static void mushroom(Entity entity) {
        if (entity instanceof MooshroomEntity) {
            Set<String> tags = entity.getCommandTags();
            if (tags.contains(".checked")) {
                return;
            }
            entity.addCommandTag(".checked");

            double num = Math.random();
            if (num < 0.5) {
                ((MooshroomEntity) entity).setVariant(MooshroomEntity.Type.BROWN);
            }
        }
    }
}
