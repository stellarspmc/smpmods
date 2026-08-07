package fun.spmc.smpmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(SculkShriekerBlockEntity.class)
public class MixinSculkShriekerBlockEntity {
    @Unique private final HashMap<BlockPos, Integer> wardenMap = new HashMap<>();
    @Shadow private int warningLevel = 0;

    @Inject(method = "trySummonWarden", at = @At("HEAD"), cancellable = true)
    public void smpmod$balanceWardenDrops(ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (wardenMap.getOrDefault(((SculkShriekerBlockEntity) (Object) this).getBlockPos(), 0) > 5) cir.cancel();
        if (warningLevel < 4) {
            wardenMap.putIfAbsent(((SculkShriekerBlockEntity) (Object) this).getBlockPos(), 0);
            wardenMap.replace(((SculkShriekerBlockEntity) (Object) this).getBlockPos(), wardenMap.get(((SculkShriekerBlockEntity) (Object) this).getBlockPos()) + 1);
        }
    }
}
