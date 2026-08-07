package fun.spmc.smpmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Pseudo
@Mixin(targets = "com.fibermc.essentialcommands.ECPerms", remap = false)
public class MixinECPerms {

    /**@Inject(method = "getHighestNumericPermission", at = @At("RETURN"))
    public int smpmod$homeModification(@NotNull CommandSourceStack source, @NotNull Identifier[] permissionGroup, CallbackInfo ci) {
        if (Arrays.stream(permissionGroup).allMatch((perm) -> perm.getNamespace().contains("essential") && perm.getPath().contains("home"))) {
            // modify homes here, too lazy to do today
        }
        return 1;
    }**/
    @ModifyReturnValue(method = "require", at = @At("RETURN"))
    public @NotNull Predicate<CommandSourceStack> smpmod$require(Predicate<CommandSourceStack> original, @NotNull Identifier permission, int defaultRequireLevel, CallbackInfo ci) {
        if (permission.getPath().contains("randomteleport")) return _ -> true;
        return original;
    }

}
