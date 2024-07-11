package fun.spmc.smpmod.minecraft.events;

import fun.spmc.smpmod.utils.RegistryEntryTool;
import fun.spmc.smpmod.utils.UtilClass;
import fun.spmc.smpmod.utils.treasure.TreasureRarities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class BlockBrokenEvent {
    public static boolean onBreakBlock(ServerWorld world, PlayerEntity player, BlockState state, BlockPos pos) {
        treasure(world, player, state, pos);
        tree(world, player, state, pos);
        return true;
    }

    private static void tree(ServerWorld world, PlayerEntity player, BlockState state, BlockPos pos) {
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
                if (UtilClass.probabilityCalc(5, player)) {
                    LightningEntity entity = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                    entity.setPos(pos.getX(), pos.getY(), pos.getZ());
                    world.spawnEntity(entity);
                    player.sendMessage(Text.literal("The gods of the trees has decided to strike you...").formatted(Formatting.DARK_RED));
                    if (UtilClass.probabilityCalc(5, player)) {
                        double random = player.getRandom().nextDouble();
                        if (random >= 0.75) {
                            ItemStack item = new ItemStack(Items.LEATHER_HELMET, 1);
                            treeTreasure(player, item);
                            player.sendMessage(Text.literal("It seems like you have been struck by the gods, so here's a helmet for you!").formatted(Formatting.BLUE));
                        } else if (random < 0.75 && random >= 0.5) {
                            ItemStack item = new ItemStack(Items.LEATHER_CHESTPLATE, 1);
                            treeTreasure(player, item);
                            player.sendMessage(Text.literal("It seems like you have been struck by the gods, so here's a chestplate for you!").formatted(Formatting.BLUE));

                        } else if (random < 0.5 && random >= 0.25) {
                            ItemStack item = new ItemStack(Items.LEATHER_LEGGINGS, 1);
                            treeTreasure(player, item);
                            player.sendMessage(Text.literal("It seems like you have been struck by the gods, so here's leggings for you!").formatted(Formatting.BLUE));

                        } else {
                            ItemStack item = new ItemStack(Items.LEATHER_BOOTS, 1);
                            treeTreasure(player, item);
                            player.sendMessage(Text.literal("It seems like you have been struck by the gods, so here's boots for you!").formatted(Formatting.BLUE));

                        }
                    }
                }
            }
        }
    }

    private static void treeTreasure(PlayerEntity player, ItemStack item) {
        item.set(DataComponentTypes.BASE_COLOR, DyeColor.byId(0));
        item.set(DataComponentTypes.TRIM, new ArmorTrim(RegistryEntryTool.getMaterial(ArmorTrimMaterials.DIAMOND), RegistryEntryTool.getPattern(ArmorTrimPatterns.SILENCE)));
        item.addEnchantment(RegistryEntryTool.getEnchantment(Enchantments.PROTECTION), 8);
        item.addEnchantment(RegistryEntryTool.getEnchantment(Enchantments.UNBREAKING), 10);
        player.getInventory().setStack(player.getInventory().getEmptySlot(), item);
    }

    private static void treasure(ServerWorld world, PlayerEntity player, BlockState state, BlockPos pos) {
        if (!(player.isCreative() || player.isInLava() || player.isClimbing())) {
            Block block = state.getBlock();

            if (block.equals(Blocks.STONE) || block.equals(Blocks.DEEPSLATE) || block.equals(Blocks.TUFF)) if (UtilClass.probabilityCalc(0.89, player)) treasures(world, player, pos);
            else if (block.equals(Blocks.DIAMOND_ORE) || block.equals(Blocks.EMERALD_ORE) || block.equals(Blocks.GOLD_ORE) || block.equals(Blocks.IRON_ORE) || block.equals(Blocks.COPPER_ORE) || block.equals(Blocks.COAL_ORE) || block.equals(Blocks.LAPIS_ORE) || block.equals(Blocks.REDSTONE_ORE)) if (UtilClass.probabilityCalc(8.9, player)) treasures(world, player, pos);
            else if (block.equals(Blocks.DEEPSLATE_DIAMOND_ORE) || block.equals(Blocks.DEEPSLATE_EMERALD_ORE) || block.equals(Blocks.DEEPSLATE_GOLD_ORE) || block.equals(Blocks.DEEPSLATE_IRON_ORE) || block.equals(Blocks.DEEPSLATE_COPPER_ORE) || block.equals(Blocks.DEEPSLATE_COAL_ORE) || block.equals(Blocks.DEEPSLATE_LAPIS_ORE) || block.equals(Blocks.DEEPSLATE_REDSTONE_ORE)) if (UtilClass.probabilityCalc(32.9, player)) treasures(world, player, pos);
        }
    }

    private static void treasures(ServerWorld world, PlayerEntity player, BlockPos pos) {
        common(world, player, pos);
        if (UtilClass.probabilityCalc(20, player)) {
            rare(world, player, pos);
            if (UtilClass.probabilityCalc(40, player)) {
                epic(world, player, pos);
                if (UtilClass.probabilityCalc(40, player)) {
                    legendary(world, player, pos);
                    if (UtilClass.probabilityCalc(15, player)) ultimate(world, player, pos);
                }
            }
        }
    }

    private static void rare(ServerWorld world, PlayerEntity player, BlockPos pos) {
        for (ItemStack item : TreasureRarities.RARE.rollStack(TreasureRarities.RARE, player)) {
            ItemEntity entity = new ItemEntity(EntityType.ITEM, world).dropStack(item);
            assert entity != null;
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
        player.sendMessage(Text.literal("and the RARE treasure."));
    }

    private static void epic(ServerWorld world, PlayerEntity player, BlockPos pos) {
        for (ItemStack item : TreasureRarities.EPIC.rollStack(TreasureRarities.EPIC, player)) {
            ItemEntity entity = new ItemEntity(EntityType.ITEM, world).dropStack(item);
            assert entity != null;
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
        player.sendMessage(Text.literal("and the EPIC treasure!"));
    }

    private static void common(ServerWorld world, PlayerEntity player, BlockPos pos) {
        for (ItemStack item : TreasureRarities.COMMON.rollStack(TreasureRarities.COMMON, player)) {
            ItemEntity entity = new ItemEntity(EntityType.ITEM, world).dropStack(item);
            assert entity != null;
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
        player.sendMessage(Text.literal("You got the basic treasure."));
    }

    private static void legendary(ServerWorld world, PlayerEntity player, BlockPos pos) {
        for (ItemStack item : TreasureRarities.LEGENDARY.rollStack(TreasureRarities.LEGENDARY, player)) {
            ItemEntity entity = new ItemEntity(EntityType.ITEM, world).dropStack(item);
            assert entity != null;
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
        player.sendMessage(Text.literal("and the LEGENDARY treasure!").formatted(Formatting.GOLD));
    }

    private static void ultimate(ServerWorld world, PlayerEntity player, BlockPos pos) {
        for (ItemStack item : TreasureRarities.ULTIMATE.rollStack(TreasureRarities.ULTIMATE, player)) {
            ItemEntity entity = new ItemEntity(EntityType.ITEM, world).dropStack(item);
            assert entity != null;
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
        player.sendMessage(Text.literal("and the ULTIMATE treasure!").formatted(Formatting.GOLD, Formatting.BOLD));
    }
}
