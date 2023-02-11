package ml.spmc.smpmod.utils.perm;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.common.base.Predicate;

/**
 * By GitHub
 */
public class Permissions {
    private static boolean fpaLoaded = false;
    private static boolean fpaLoadedChecked = false;

    private static boolean isFPAPILoaded() {
        if (!fpaLoadedChecked) {
            fpaLoadedChecked = true;
            fpaLoaded = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        }
        return fpaLoaded;
    }

    public static Predicate<CommandSourceStack> require(@NotNull String permission, boolean defaultValue) {
        if (isFPAPILoaded()) {
            return PermissionsExecutor.require(permission, defaultValue);
        }
        return _ignored -> defaultValue;
    }

    public static Predicate<CommandSourceStack> require(@NotNull String permission, int level) {
        if (isFPAPILoaded()) {
            return PermissionsExecutor.require(permission, level);
        }
        return player -> player.hasPermission(level);
    }

    public static boolean check(@NotNull CommandSourceStack source, @NotNull String permission, boolean fallback) {
        if (isFPAPILoaded()) {
            return PermissionsExecutor.check(source, permission, fallback);
        }
        return fallback;
    }

    public static boolean check(@NotNull CommandSourceStack source, @NotNull String permission, int level) {
        if (isFPAPILoaded()) {
            return PermissionsExecutor.check(source, permission, level);
        }
        return source.hasPermission(level);
    }

    public static boolean check(@NotNull Entity entity, @NotNull String permission, boolean fallback) {
        if (isFPAPILoaded()) {
            return PermissionsExecutor.check(entity, permission, fallback);
        }
        return fallback;
    }

    public static boolean check(@NotNull Entity entity, @NotNull String permission, int level) {
        if (isFPAPILoaded()) {
            return PermissionsExecutor.check(entity, permission, level);
        }
        return entity.hasPermissions(level);
    }
}
