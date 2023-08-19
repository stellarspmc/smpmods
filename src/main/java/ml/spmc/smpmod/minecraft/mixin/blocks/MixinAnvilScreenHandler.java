package ml.spmc.smpmod.minecraft.mixin.blocks;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AnvilScreenHandler.class)
public class MixinAnvilScreenHandler {

    @Final
    @Shadow
    private Property levelCost;
    @Shadow
    private int repairItemUsage;

    /**
     * @author tcfplayz
     * @reason make anvil costs cheaper
     */
    @Overwrite
    protected void onTakeOutput(PlayerEntity playerEntity, ItemStack itemStack) {
        int cost = Math.floorDiv(this.levelCost.get(), 3);
        if (!playerEntity.getAbilities().creativeMode) playerEntity.addExperienceLevels(-cost);
        playerEntity.getInventory().setStack(0, ItemStack.EMPTY);
        if (this.repairItemUsage > 0) {
            ItemStack itemStack2 = playerEntity.getInventory().getStack(1);
            if (!itemStack2.isEmpty() && itemStack2.getCount() > this.repairItemUsage) {
                itemStack2.decrement(this.repairItemUsage);
                playerEntity.getInventory().setStack(1, itemStack2);
            } else playerEntity.getInventory().setStack(1, ItemStack.EMPTY);
        } else playerEntity.getInventory().setStack(1, ItemStack.EMPTY);


        this.levelCost.set(0);
        ScreenHandlerContext context = ScreenHandlerContext.EMPTY;
        context.run((world, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (!playerEntity.getAbilities().creativeMode && blockState.isIn(BlockTags.ANVIL) && playerEntity.getRandom().nextFloat() < 0.12F) {
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock(blockPos, false);
                    world.syncWorldEvent(1029, blockPos, 0);
                } else {
                    world.setBlockState(blockPos, blockState2, 2);
                    world.syncWorldEvent(1030, blockPos, 0);
                }
            } else {
                world.syncWorldEvent(1030, blockPos, 0);
            }

        });
    }

    /**
     * @author tcfplayz
     * @reason make anvil costs cheaper
     */
    @Overwrite
    public static int getNextCost(int i) {
        return i;
    }

}
