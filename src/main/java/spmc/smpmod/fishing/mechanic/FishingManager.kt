package spmc.smpmod.fishing.mechanic;

import spmc.smpmod.fishing.RodItem;
import spmc.smpmod.fishing.RodTiers;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FishingManager {
    private static final Map<UUID, FishingSession> ACTIVE_SESSIONS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(_ -> ACTIVE_SESSIONS.entrySet().removeIf(entry -> entry.getValue().tick()));
    }

    public static void startMinigame(ServerPlayer player, FishingHook hook) {
        if (ACTIVE_SESSIONS.containsKey(player.getUUID())) return;

        RodTiers tier = RodTiers.NORMAL;
        if (player.getMainHandItem().getItem() instanceof RodItem customRod) tier = customRod.getTier();
        ACTIVE_SESSIONS.put(player.getUUID(), new FishingSession(player, hook, tier));
    }

    public static boolean isFishing(UUID playerUuid) { return ACTIVE_SESSIONS.containsKey(playerUuid); }
    public static void cancelMinigame(UUID playerUuid) { ACTIVE_SESSIONS.remove(playerUuid); }
}