package fun.spmc.smpmod.mixin;

import fun.spmc.smpmod.fishing.mechanic.FishingManager;
import fun.spmc.smpmod.fishing.rod.RodItem;
import fun.spmc.smpmod.fishing.rod.RodTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook {
    @Shadow private int nibble;
    @Shadow public abstract @Nullable Player getPlayerOwner();

    @Inject(method = "catchingFish", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;nibble:I", ordinal = 1, opcode = Opcodes.GETFIELD))
    public void smpmod$catchingFishOverride(BlockPos blockPos, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        if (!hook.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) getPlayerOwner();
            if (player != null) {
                if (player.getMainHandItem().getItem() instanceof RodItem) {
                    this.nibble = 100;
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

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean smpmod$supportLavaAndVoidFluids(FluidState instance, TagKey<Fluid> tagKey) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) getPlayerOwner();
        if (player != null) {
            if (player.getMainHandItem().getItem() instanceof RodItem item) {
                if (tagKey == FluidTags.WATER) {
                    if (item.getTier().equals(RodTiers.NETHERITE) && (instance.is(FluidTags.WATER) || instance.is(FluidTags.LAVA))) return true;
                    if (item.getTier().equals(RodTiers.NETHERITE) && hook.level().dimension() == ServerLevel.END && hook.getY() < 0) return true;
                }
            }
        }
        return instance.is(tagKey);
    }

    // 2. Prevent the hook from burning in lava or void-despawning
    @Inject(method = "tick", at = @At("HEAD"))
    private void smpmod$preventHookDestruction(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) getPlayerOwner();
        if (player != null) {
            if (player.getMainHandItem().getItem() instanceof RodItem item) {
                if (item.getTier().equals(RodTiers.NETHERITE) && hook.isInLava()) hook.clearFire();
                if (item.getTier().equals(RodTiers.NETHERITE) && hook.level().dimension() == ServerLevel.END && hook.getY() < 0) {
                    Vec3 vel = hook.getDeltaMovement();
                    hook.setDeltaMovement(vel.x * 0.8, Math.max(vel.y * 0.5, -0.02), vel.z * 0.8);
                }
            }
        }
    }
}
