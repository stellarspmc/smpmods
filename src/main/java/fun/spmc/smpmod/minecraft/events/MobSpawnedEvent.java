package fun.spmc.smpmod.minecraft.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.Set;

public class MobSpawnedEvent {
    public static void onEntityJoin(Entity entity) {
        mushroom(entity);
        creeper(entity);
    }

    private static void mushroom(Entity entity) {
        if (entity instanceof MooshroomEntity) {
            Set<String> tags = entity.getCommandTags();
            if (tags.contains(".checked")) return;

            entity.addCommandTag(".checked");

            double num = Math.random();
            if (num < 0.5) {
                ((MooshroomEntity) entity).setVariant(MooshroomEntity.Type.BROWN);
            }
        }
    }

    private static void creeper(Entity entity) {
        if (entity instanceof CreeperEntity) {
            Set<String> tags = entity.getCommandTags();
            if (tags.contains(".checked")) return;
            entity.addCommandTag(".checked");

            double num = Math.random();
            if (num < 0.3) {
                NbtCompound nbt = new NbtCompound();
                nbt.putBoolean("powered", true);
                entity.writeNbt(nbt);
            }
        }
    }
}
