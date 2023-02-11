package ml.spmc.smpmod.minecraft.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class TickMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void tickServerMixin(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        //UtilClass.ah.tick();
    }
}
