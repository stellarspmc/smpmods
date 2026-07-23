package fun.spmc.smpmod.minecraft.mixin.shop;

import fun.spmc.smpmod.minecraft.economy.shop.ShopManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class MixinHopperBlockEntity {

    @Inject(method = "getContainerAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/Container;", at = @At("HEAD"), cancellable = true)
    private static void smpmods$blockShopSteal(Level level, BlockPos pos, CallbackInfoReturnable<Container> cir) {
        if (ShopManager.getByPos((ServerLevel) level, pos) != null) cir.setReturnValue(null);
    }
}
