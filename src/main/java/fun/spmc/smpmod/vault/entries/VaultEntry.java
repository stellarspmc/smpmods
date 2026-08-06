package fun.spmc.smpmod.vault.entries;

import net.minecraft.server.level.ServerLevel;

public interface VaultEntry {
    String id();
    double value();
    void apply(ServerLevel level);
}