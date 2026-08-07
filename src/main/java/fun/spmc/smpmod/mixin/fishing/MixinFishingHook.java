package fun.spmc.smpmod.mixin.fishing;

import fun.spmc.smpmod.fishing.mechanic.FishingManager;
import fun.spmc.smpmod.fishing.rod.RodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public class MixinFishingHook {
    @Shadow private int nibble;

    @Inject(method = "catchingFish", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;nibble:I", ordinal = 1, opcode = Opcodes.GETFIELD))
    public void smpmod$catchingFishOverride(BlockPos blockPos, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        if (!hook.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
            if (player != null) {
                if (player.getMainHandItem().getItem() instanceof RodItem) {
                    this.nibble = 400;
                    FishingManager.startMinigame(player, hook);
                }
            }
        }
    }

    @Inject(method = "shouldStopFishing", at = @At("HEAD"), cancellable = true)
    private void smpmod$allowCustomFishingRods(Player owner, CallbackInfoReturnable<Boolean> cir) {
        FishingHook hook = (FishingHook) (Object) this;

        if (owner.canInteractWithLevel()) {
            ItemStack mainHand = owner.getMainHandItem();
            ItemStack offHand = owner.getOffhandItem();

            boolean mainHandIsFishing = mainHand.getItem() instanceof FishingRodItem;
            boolean offHandIsFishing = offHand.getItem() instanceof FishingRodItem;

            if ((mainHandIsFishing || offHandIsFishing) && hook.distanceToSqr(owner) <= 1024.0) {
                cir.setReturnValue(false);
                return;
            }
        }

        hook.discard();
        cir.setReturnValue(true);
    }
}
