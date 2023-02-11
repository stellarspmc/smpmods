package ml.spmc.smpmod.minecraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(at = @At("HEAD"), method = "onBreak()V")
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        player.sendMessage(new LiteralText("You can't break blocks here"), true);
        info.cancel();
    }
}
