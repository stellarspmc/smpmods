package fun.spmc.smpmod.mixin.fishing;

import fun.spmc.smpmod.fishing.mechanic.FishingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class MixinFishingHook {
    @Shadow private int nibble;

    @Inject(method = "catchingFish", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;nibble:I", ordinal = 0, opcode = Opcodes.GETFIELD))
    public void smpmod$catchingFishOverride(BlockPos blockPos, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;

        if (!hook.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
            if (player != null) {
                this.nibble = 400;
                FishingManager.startMinigame(player, hook);
            }
        }
    }
}
