package ml.spmc.smpmod.minecraft.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Shadow public abstract Random getRandom();

    @Shadow protected float lastDamageTaken;

    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void onDamaged(DamageSource damageSource, CallbackInfo ci) {
        Entity entity = damageSource.getSource();
        if (entity != null) {
            ArmorStandEntity indicator = new ArmorStandEntity(entity.getWorld(), entity.getX() + getRandom().nextBetween(-1 ,1) * getRandom().nextDouble(), entity.getY() - 1 + getRandom().nextBetween(-1 ,1) * getRandom().nextDouble(), entity.getZ() + getRandom().nextBetween(-1 ,1) * getRandom().nextDouble());
            indicator.setInvisible(true);
            indicator.setCustomNameVisible(true);
            indicator.setCustomName(Text.literal("-" + lastDamageTaken).formatted(Formatting.RED));
        }
    }
}
