package fun.spmc.smpmod.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Unique
    private static boolean boostActive = false;
    @Unique
    private static double boostValue = 1f;

    @ModifyVariable(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true, name = "newEffect")
    private MobEffectInstance smp$boostPotionEffects(MobEffectInstance newEffect) {
        if ((Object) this instanceof ServerPlayer) {
            if (boostActive) {
                return new MobEffectInstance(
                        newEffect.getEffect(),
                        (int) (newEffect.getDuration() * boostValue),
                        newEffect.getAmplifier(),
                        newEffect.isAmbient(),
                        newEffect.isVisible(),
                        newEffect.showIcon()
                );
            }
        }
        return newEffect;
    }

    @Unique
    public void startBoost(double value) {
        boostValue += value;
        boostActive = true;
    }

    @Unique
    public void endBoost() {
        boostActive = false;
        boostValue = 1f;
    }
}
