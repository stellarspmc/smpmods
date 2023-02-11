package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.minecraft.callback.ServerTickCallback;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        ServerTickCallback.EVENT.invoker().onTick();
    }
}
