package fun.spmc.smpmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkShriekerBlockEntity.class)
public class MixinSculkShriekerBlockEntity {
    @Unique private int smpmod$spawnedWardenCount = 0;

    @Inject(method = "trySummonWarden", at = @At("HEAD"), cancellable = true)
    private void smpmod$limitWardenSpawns(ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (this.smpmod$spawnedWardenCount >= 5) cir.setReturnValue(false);
    }

    @Inject(method = "trySummonWarden", at = @At("RETURN"))
    private void smpmod$trackWardenSpawn(ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) this.smpmod$spawnedWardenCount++;
    }
}
