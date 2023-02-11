package ml.spmc.smpmod.utils.perm;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.common.base.Predicate;

class PermissionsExecutor {
    static Predicate<CommandSourceStack> require(@NotNull String permission, boolean defaultValue) {
        return Permissions.require(permission, defaultValue);
    }

    static Predicate<CommandSourceStack> require(@NotNull String permission, int level) {
        return Permissions.require(permission, level);
    }

    static boolean check(@NotNull CommandSourceStack source, @NotNull String permission, boolean fallback) {
        return Permissions.check(source, permission, fallback);
    }

    static boolean check(@NotNull CommandSourceStack source, @NotNull String permission, int level) {
        return Permissions.check(source, permission, level);
    }

    static boolean check(@NotNull Entity entity, @NotNull String permission, boolean fallback) {
        return Permissions.check(entity, permission, fallback);
    }

    static boolean check(@NotNull Entity entity, @NotNull String permission, int level) {
        return Permissions.check(entity, permission, level);
    }
}
