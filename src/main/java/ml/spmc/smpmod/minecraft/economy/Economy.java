package ml.spmc.smpmod.minecraft.economy;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class Economy {

    // cmd util
    private static void createItem(Player player, int big) {
        ItemStack itemStack = new ItemStack(Items.SUNFLOWER);
        itemStack.setCount(big);
        CompoundTag tag = itemStack.getOrCreateTagElement("worth");
        tag.putDouble("worth", 1);
        itemStack.setTag(tag);
        itemStack.enchant(Enchantments.UNBREAKING, 1);
        itemStack.setHoverName(Component.literal("$" + 1).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ItemEntity itemEntity = player.drop(itemStack, true);
        assert itemEntity != null;
        itemEntity.setNoPickUpDelay();
        itemEntity.setOwner(player.getUUID());
    }

    // cmds
    public static int depositCommand(CommandContext<CommandSourceStack> css, ServerPlayer plr)  {
        try {
            Inventory inv = plr.getInventory();
            for (ItemStack item : inv.items) {
                if (item.getItem().equals(Items.DIAMOND)) {
                    UtilClass.getDatabaseManager().changeBalance(plr.getName().getString(), 1);
                    inv.removeItem(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int withdrawCommand(CommandContext<CommandSourceStack> e, double amount) {
        try {
            Player plr = e.getSource().getPlayerOrException();
            createItem(plr, ((int) Math.floor(amount)));
            UtilClass.getDatabaseManager().changeBalance(plr.getName().getString(), -((int) Math.floor(amount)));
        } catch (CommandSyntaxException ex) {
            throw new RuntimeException(ex);
        }
        return 0;
    }
}
