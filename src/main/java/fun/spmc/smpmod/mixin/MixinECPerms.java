package fun.spmc.smpmod.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Pseudo
@Mixin(targets = "com.fibermc.essentialcommands.ECPerms", remap = false)
public class MixinECPerms {

    @Inject(method = "getHighestNumericPermission", at = @At("RETURN"))
    public int smpmod$homeModification(@NotNull CommandSourceStack source, @NotNull Identifier[] permissionGroup,  CallbackInfo ci) {
        if (Arrays.stream(permissionGroup).allMatch((perm) -> perm.getNamespace().contains("essential") && perm.getPath().contains("home"))) {
            // modify homes here, too lazy to do today
        }
        return 1;
    }

}
