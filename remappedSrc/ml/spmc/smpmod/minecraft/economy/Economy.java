package ml.spmc.smpmod.minecraft.economy;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.utils.UtilClass;
import ml.spmc.smpmod.utils.sql.DatabaseManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Economy {

    // cmd util
    private static void dropItem(String type, int amount, Player player) {
        switch (type) {
            case "netherite":
                int count = (int) Math.floor((double)amount/EconomyValues.netherite.value);
                createItem(player, Items.NETHERITE_INGOT, count);
                player.sendSystemMessage(Component.literal("Withdrawn " + count + " netherite"));
                break;
            case "diamond":
                count = (int) Math.floor((double)amount/EconomyValues.diamond.value);
                createItem(player, Items.DIAMOND, count);
                player.sendSystemMessage(Component.literal("Withdrawn " + count + " diamond/s"));
                break;
            case "gold":
                count = (int) Math.floor((double)amount/EconomyValues.gold.value);
                createItem(player, Items.GOLD_INGOT, count);
                player.sendSystemMessage(Component.literal("Withdrawn " + count + " gold"));
                break;
            case "iron":
                count = amount;
                createItem(player, Items.IRON_INGOT, count);
                player.sendSystemMessage(Component.literal("Withdrawn " + count + " iron"));
                break;
            default: player.sendSystemMessage(Component.literal("This withdraw system doesn't support minerals below coal / Removed block withdraws / Doesn't exist."));
        }
    }

    private static void createItem(Player player, Item item, int count) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.setCount(count);
        ItemEntity itemEntity = player.drop(itemStack, true);
        assert itemEntity != null;
        itemEntity.setNoPickUpDelay();
        itemEntity.setOwner(player.getUUID());
    }
    private static int itemIterate(int amount, Player player) {
        for (int i = 0; i < amount; i++) {
            if (amount >= EconomyValues.netherite.value) {
                ItemEntity itemEntity = player.drop(new ItemStack(Items.NETHERITE_INGOT), true);
                assert itemEntity != null;
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(player.getUUID());
                amount -= EconomyValues.netherite.value;
            } else {
                if (amount >= EconomyValues.diamond.value) {
                    ItemEntity itemEntity = player.drop(new ItemStack(Items.DIAMOND), true);
                    assert itemEntity != null;
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setOwner(player.getUUID());
                    amount -= EconomyValues.diamond.value;
                } else {
                    if (amount >= EconomyValues.gold.value) {
                        ItemEntity itemEntity = player.drop(new ItemStack(Items.GOLD_INGOT), true);
                        assert itemEntity != null;
                        itemEntity.setNoPickUpDelay();
                        itemEntity.setOwner(player.getUUID());
                        amount -= EconomyValues.gold.value;
                    } else {
                        if (amount >= EconomyValues.iron.value) {
                            ItemEntity itemEntity = player.drop(new ItemStack(Items.IRON_INGOT), true);
                            assert itemEntity != null;
                            itemEntity.setNoPickUpDelay();
                            itemEntity.setOwner(player.getUUID());
                            amount -= EconomyValues.iron.value;
                        } else {
                            if (amount >= EconomyValues.coal.value) {
                                ItemEntity itemEntity = player.drop(new ItemStack(Items.COAL), true);
                                assert itemEntity != null;
                                itemEntity.setNoPickUpDelay();
                                itemEntity.setOwner(player.getUUID());
                                amount -= EconomyValues.coal.value;
                            } else {
                                if (amount >= EconomyValues.emerald.value) {
                                    ItemEntity itemEntity = player.drop(new ItemStack(Items.EMERALD), true);
                                    assert itemEntity != null;
                                    itemEntity.setNoPickUpDelay();
                                    itemEntity.setOwner(player.getUUID());
                                    amount -= EconomyValues.emerald.value;
                                } else {
                                    ItemEntity itemEntity = player.drop(new ItemStack(Items.COPPER_INGOT), true);
                                    assert itemEntity != null;
                                    itemEntity.setNoPickUpDelay();
                                    itemEntity.setOwner(player.getUUID());
                                    amount -= EconomyValues.copper.value;
                                }
                            }
                        }
                    }
                }
            }
        }
        return amount;
    }

    private static void dropItem(int amount, Player player) {
        while (amount > 0) amount = itemIterate(amount, player);
    }

    // cmds
    public static int depositCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        if (UtilClass.lockdown) {
            player.sendSystemMessage(Component.literal("Economy in lockdown!"));
            return 0;
        }
        DatabaseManager dm = UtilClass.getDatabaseManager();
        int currencyCount = 0;
        for (int j = 0; j < 36; j++) {
            Item item = player.getInventory().getItem(j).getItem();
            ItemStack itemStack = player.getInventory().getItem(j);
            if (item.equals(Items.COAL)) currencyCount += itemStack.getCount() * EconomyValues.coal.value;
            else if (item.equals(Items.EMERALD)) currencyCount += itemStack.getCount() * EconomyValues.emerald.value;
            else if (item.equals(Items.COPPER_INGOT)) currencyCount += itemStack.getCount() * EconomyValues.copper.value;
            else if (item.equals(Items.IRON_INGOT)) currencyCount += itemStack.getCount() * EconomyValues.iron.value;
            else if (item.equals(Items.GOLD_INGOT)) currencyCount += itemStack.getCount() * EconomyValues.gold.value;
            else if (item.equals(Items.DIAMOND)) currencyCount += itemStack.getCount() * EconomyValues.diamond.value;
            else if (item.equals(Items.NETHERITE_INGOT)) currencyCount += itemStack.getCount() * EconomyValues.netherite.value;
            else if (item.equals(Items.COAL_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.coal.value * 9;
            else if (item.equals(Items.EMERALD_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.emerald.value * 9;
            else if (item.equals(Items.COPPER_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.copper.value * 9;
            else if (item.equals(Items.IRON_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.iron.value * 9;
            else if (item.equals(Items.GOLD_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.gold.value * 9;
            else if (item.equals(Items.DIAMOND_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.diamond.value * 9;
            else if (item.equals(Items.NETHERITE_BLOCK)) currencyCount += itemStack.getCount() * EconomyValues.netherite.value * 9;
            else if (item.equals(Items.NETHER_STAR)) currencyCount += itemStack.getCount() * EconomyValues.nether_star.value;
            if (currencyCount > 0) player.getInventory().setItem(j, new ItemStack(Items.AIR));
        }
        if (System.currentTimeMillis() / 100000 < currencyCount) ctx.getSource().sendFailure(Component.literal("It is over deposit maximum!"));
        if (dm.changeBalance(player.getName().getString(), currencyCount)) ctx.getSource().sendSuccess(Component.literal("Added $" + currencyCount + " to your account"), false);
        else Economy.dropItem(currencyCount, player);
        return 1;
    }

    public static int withdrawSpec(CommandContext<CommandSourceStack> ctx, String type, int amount) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        if (UtilClass.lockdown) {
            player.sendSystemMessage(Component.literal("Economy in lockdown!"));
            return 0;
        } DatabaseManager dm = UtilClass.getDatabaseManager();
        if (amount > 2304000)  {
            ctx.getSource().sendFailure(Component.literal("Over withdraw maximum!"));
            return 0;
        } if (dm.getBalance(player.getName().getString()) > amount) {
            dm.changeBalance(player.getName().getString(), -amount);
            dropItem(type.toLowerCase(), amount, player);
            ctx.getSource().sendSuccess(Component.literal("Withdrawn $" + amount), false);
        } else ctx.getSource().sendFailure(Component.literal("Sorry, but you have less than $" + amount));
        return 1;
    }

    public static int withdrawCommand(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        if (UtilClass.lockdown) {
            player.sendSystemMessage(Component.literal("Economy in lockdown!"));
            return 0;
        } DatabaseManager dm = UtilClass.getDatabaseManager();
        if (amount > 2304000)  {
            ctx.getSource().sendFailure(Component.literal("Over withdraw maximum!"));
            return 0;
        } if (dm.changeBalance(player.getName().getString(), -amount)) {
            dropItem(amount, player);
            ctx.getSource().sendSuccess(Component.literal("Withdrawn $" + amount), false);
        } else ctx.getSource().sendFailure(Component.literal("Sorry, but you have less than $" + amount));
        return 1;
    }
}
