package fun.spmc.smpmod.minecraft.treasure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TreasureFatigue {
    private static final Map<UUID, Deque<Long>> HISTORY = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 600 * 1000;

    public static double getMultiplier(UUID playerUUID) {
        Deque<Long> timestamps = HISTORY.get(playerUUID);
        if (timestamps == null) return 1.0;
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) timestamps.pollFirst();

            int count = timestamps.size();
            if (count == 0) {
                HISTORY.remove(playerUUID, timestamps);
                return 1;
            }

            return Math.pow(0.85, count);
        }
    }

    public static void recordTreasure(UUID playerUUID) {
        long now = System.currentTimeMillis();
        long cutoff = now - WINDOW_MS;
        Deque<Long> timestamps = HISTORY.computeIfAbsent(playerUUID, _ -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) timestamps.pollFirst();
            timestamps.addLast(now);
        }
    }
}