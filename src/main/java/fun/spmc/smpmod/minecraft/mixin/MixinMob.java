package fun.spmc.smpmod.minecraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MixinMob {

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void smpmods$despawn(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        if (mob.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getForceLoadedChunks().contains(mob.chunkPosition().pack())) ci.cancel();
        }
    }
}
