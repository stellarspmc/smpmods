package ml.spmc.smpmod.minecraft.mixin.entity;

import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void onDamaged(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) damageSource.getSource();
        Entity a = damageSource.getAttacker();
        if (a instanceof PlayerEntity attacker && entity != null) {
            if (UtilClass.probabilityCalc(15, attacker))
                entity.setHealth((float) (entity.getHealth() * 1.25));
        }
    }
}
