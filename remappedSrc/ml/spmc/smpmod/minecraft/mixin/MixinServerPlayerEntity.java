package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.minecraft.callback.PlayerDeathCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerEntity {

    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci){
        ServerPlayer serverPlayerEntity = (ServerPlayer) (Object) this;
        PlayerDeathCallback.EVENT.invoker().onPlayerDeath(serverPlayerEntity, serverPlayerEntity.getCombatTracker().getDeathMessage());
    }

}
