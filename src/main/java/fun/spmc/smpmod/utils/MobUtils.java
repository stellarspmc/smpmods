package fun.spmc.smpmod.utils;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class MobUtils {
    public static ServerLevel getLevelOfEntity(UUID uuid) {
        final AtomicReference<ServerLevel> level = new AtomicReference<>();
        minecraftServer.getAllLevels().forEach((a) -> {
            if (a.getEntity(uuid) != null) level.set(a);
        });
        return level.get();
    }
}
