package ml.spmc.smpmod.minecraft.economy;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Economy {

    // cmd util
    private static void createItem(PlayerEntity player, int big) {
        ItemStack itemStack = new ItemStack(Items.SUNFLOWER);
        itemStack.setCount(big);
        NbtCompound tag = itemStack.getOrCreateNbt();
        tag.putDouble("worth", 1);
        itemStack.setNbt(tag);
        itemStack.addEnchantment(Enchantments.UNBREAKING, 1);
        itemStack.setCustomName(Text.literal("$1").formatted(Formatting.BOLD, Formatting.AQUA));
        ItemEntity itemEntity = player.dropItem(itemStack, true);
        assert itemEntity != null;
        itemEntity.setPickupDelayInfinite();
        itemEntity.setOwner(player.getUuid());
    }

    // cmds
    public static int depositCommand(CommandContext<ServerCommandSource> css, PlayerEntity plr)  {
        try {
            Inventory inv = plr.getInventory();
            for (int i=0; i>inv.size();i++) {
                if (inv.getStack(i).equals(Items.DIAMOND)) {
                    //UtilClass.getDatabaseManager().changeBalance(plr.getName().getString(), 1);
                    inv.removeStack(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int withdrawCommand(CommandContext<ServerCommandSource> e, double amount) {
        try {
            PlayerEntity plr = e.getSource().getPlayerOrThrow();
            createItem(plr, ((int) Math.floor(amount)));
            //UtilClass.getDatabaseManager().changeBalance(plr.getName().getString(), -((int) Math.floor(amount)));
        } catch (CommandSyntaxException ex) {
            throw new RuntimeException(ex);
        }
        return 0;
    }
}
