package fun.spmc.smpmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fun.spmc.smpmod.fishing.mechanic.FishingManager;
import fun.spmc.smpmod.fishing.RodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
    @Shadow protected abstract void catchingFish(BlockPos blockPos);

    @ModifyExpressionValue(
            method = "catchingFish",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/state/BlockState.is (Ljava/lang/Object;)Z")
    )
    private boolean smpmod$allowLavaAndVoid(boolean originalIsWater) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();

        if (player != null && player.getMainHandItem().getItem() instanceof RodItem rod) {
            if (rod.canLavaFish() && hook.isInLava()) return true;
            if (rod.canVoidFish() && hook.level().dimension() == ServerLevel.END && hook.getY() < 0) return true;
        }

        return originalIsWater;
    }

    @WrapOperation(
            method = "catchingFish",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I")
    )
    private int smpmod$swapParticleTypes(ServerLevel instance, ParticleOptions particle, double x, double y, double z, int count, double xDist, double yDist, double zDist, double speed, Operation<Integer> original) {
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.isInLava()) particle = ParticleTypes.FLAME;
        else if (hook.getY() < 0) particle = ParticleTypes.PORTAL;
        return original.call(instance, particle, x, y, z, count, xDist, yDist, zDist, speed);
    }

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
                    if (item.canLavaFish() && (instance.is(FluidTags.WATER) || instance.is(FluidTags.LAVA))) return true;
                    if (item.canVoidFish() && hook.level().dimension() == ServerLevel.END && hook.getY() < 0) return true;
                }
            }
        }
        return instance.is(tagKey);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void smpmod$preventHookDestruction(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) getPlayerOwner();
        if (player != null) {
            if (player.getMainHandItem().getItem() instanceof RodItem item) {
                if (item.canLavaFish() && hook.isInLava()) hook.clearFire();
                if (item.canVoidFish() && hook.level().dimension() == ServerLevel.END) {
                    if (hook.getY() < -4) {
                        if (!hook.isNoGravity()) hook.setNoGravity(true);
                        Vec3 vel = hook.getDeltaMovement();
                        hook.setDeltaMovement(vel.x * 0.8, 0.0, vel.z * 0.8);
                    }
                    if (!hook.level().isClientSide()) catchingFish(hook.blockPosition());
                }
            }
        }
    }
}
