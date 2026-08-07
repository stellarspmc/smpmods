package fun.spmc.smpmod.mixin;

import fun.spmc.smpmod.vault.VaultUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true, index = 1)
    private MobEffectInstance smp$boostPotionEffects(MobEffectInstance newEffect) {
        if ((Object) this instanceof ServerPlayer) {
            if (VaultUtils.buffValue != 0f) {
                return new MobEffectInstance(
                        newEffect.getEffect(),
                        (int) (newEffect.getDuration() * (1.0 + VaultUtils.buffValue)),
                        newEffect.getAmplifier(),
                        newEffect.isAmbient(),
                        newEffect.isVisible(),
                        newEffect.showIcon()
                );
            }
        }
        return newEffect;
    }
}