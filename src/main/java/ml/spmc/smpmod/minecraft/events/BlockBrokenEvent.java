package ml.spmc.smpmod.minecraft.events;

import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BlockBrokenEvent {
    public static boolean onBreakBlock(ServerWorld world, PlayerEntity player, BlockState state) {
        treasure(world, player, state);
        tree(world, player, state);
        return true;
    }

    private static void tree(ServerWorld world, PlayerEntity player, BlockState state) {
        if (!(player.isCreative() || player.isInLava() || player.isClimbing())) {
            Block block = state.getBlock();
            if (block.equals(Blocks.OAK_LOG) ||
                    block.equals(Blocks.BIRCH_LOG) ||
                    block.equals(Blocks.SPRUCE_LOG) ||
                    block.equals(Blocks.DARK_OAK_LOG) ||
                    block.equals(Blocks.JUNGLE_LOG) ||
                    block.equals(Blocks.ACACIA_LOG) ||
                    block.equals(Blocks.MANGROVE_LOG) ||
                    block.equals(Blocks.CHERRY_LOG)) {
                if (player.getRandom().nextDouble() >= 0.95) {
                    LightningEntity entity = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                    entity.setPos(player.getX(), player.getY(), player.getZ());
                    world.spawnEntity(entity);
                    player.sendMessage(Text.literal("The gods of the trees has decided to strike you...").formatted(Formatting.DARK_RED));
                }
            }
        }
    }

    private static void treasure(ServerWorld world, PlayerEntity player, BlockState state) {
        if (!(player.isCreative() || player.isInLava() || player.isClimbing())) {
            Block block = state.getBlock();

            if (block.equals(Blocks.STONE) || block.equals(Blocks.DEEPSLATE) || block.equals(Blocks.TUFF)) {
                if (UtilClass.probabilityCalc(0.89, player)) {
                    player.dropItem(new ItemStack(Items.COAL, 50).getItem());
                    player.sendMessage(Text.literal("You got the basic treasure."));
                }
            } else if (state.getBlock().equals(Blocks.DIAMOND_ORE)) {

            }
        }
    }
}
