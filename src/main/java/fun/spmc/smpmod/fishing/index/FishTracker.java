package fun.spmc.smpmod.fishing.index;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.CustomData;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FishTracker {
    /**public static Set<String> getUnlockedFish(ServerPlayer player) {
        Set<String> unlocked = new HashSet<>();
        Optional<CompoundTag> tag = player.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getCompound("unlocked_fish");

        tag.ifPresent(compoundTag -> compoundTag.getAllKeys().forEach(unlocked::add));
        return unlocked;
    }

    public static void unlockFish(ServerPlayer player, String fishId) {
        CustomData.update(DataComponents.CUSTOM_DATA, player, tag -> {
            CompoundTag unlocked = tag.getCompound("unlocked_fish").orElse(new CompoundTag());
            unlocked.putBoolean(fishId, true);
            tag.put("unlocked_fish", unlocked);
        });
    }*/
}