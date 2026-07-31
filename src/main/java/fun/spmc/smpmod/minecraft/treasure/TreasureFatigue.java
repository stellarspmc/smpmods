package fun.spmc.smpmod.minecraft.treasure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TreasureFatigue {
    private static final Map<UUID, List<Long>> HISTORY = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 600 * 1000;

    public static double getMultiplier(UUID playerUUID) {
        long now = System.currentTimeMillis();
        List<Long> timestamps = HISTORY.computeIfAbsent(playerUUID, _ -> new ArrayList<>());

        synchronized (timestamps) {
            timestamps.removeIf(time -> (now - time) > WINDOW_MS);

            int recentCount = timestamps.size();
            return Math.pow(0.85, recentCount);
        }
    }

    public static void recordTreasure(UUID playerUUID) {
        List<Long> timestamps = HISTORY.computeIfAbsent(playerUUID, _ -> new ArrayList<>());
        synchronized (timestamps) {
            timestamps.add(System.currentTimeMillis());
        }
    }
}