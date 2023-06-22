package ml.spmc.smpmod.minecraft.events;

import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class TreasureEvent {
    public static boolean onBreakBlock(ServerWorld world, PlayerEntity player, BlockState state) {
        if (!(player.isCreative() || player.isInLava() || player.isClimbing())) {
            Block block = state.getBlock();

            if (block.equals(Blocks.STONE) || block.equals(Blocks.DEEPSLATE) || block.equals(Blocks.TUFF)) {
                if (UtilClass.probabilityCalc(0.89)) {
                    player.dropItem(new ItemStack(Items.COAL, 50).getItem());
                    player.sendMessage(Text.literal("You got the basic treasure."));
                }
            } else if (state.getBlock().equals(Blocks.DIAMOND_ORE)) {

            }
        }
        return true;
    }
}
