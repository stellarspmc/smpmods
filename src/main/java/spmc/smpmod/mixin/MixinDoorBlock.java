package spmc.smpmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public class MixinDoorBlock {

    @Unique private boolean enabled = false; // TODO: add player enable / disable functionality

    @Inject(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DoorBlock;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Z)V"))
    public void use(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (enabled) { // use player to get data...
            BlockState[] neighbourDoors = new BlockState[]{level.getBlockState(pos.north()), level.getBlockState(pos.south()), level.getBlockState(pos.east()), level.getBlockState(pos.west())};
            for (int i = 0; i < neighbourDoors.length; i++) {
                var door = neighbourDoors[i];
                if (door.getBlock() instanceof DoorBlock) level.setBlock(translator(pos, i), door.setValue(DoorBlock.OPEN, !door.getValue(DoorBlock.OPEN)), 10);
            }
        }
    }

    @Unique
    private BlockPos translator(BlockPos pos, int index) {
        return switch (index) {
            case 1 -> pos.north();
            case 2 -> pos.south();
            case 3 -> pos.east();
            case 4 -> pos.west();
            default -> pos;
        };
    }
}
