package fun.spmc.smpmod.industrial.mineral;

import fun.spmc.smpmod.utils.SimplerPolymerItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SyntheticMineral extends SimplerPolymerItem {
    public SyntheticMineral(Properties properties, Item vanillaItem) {
        super(properties.stacksTo(64), vanillaItem);
    }

    @Override public Component buildName(ItemStack stack) {
        return null;
    }
    @Override public List<Component> buildLore(ItemStack stack) {
        return List.of();
    }
    @Override public void modifyItem(ItemStack stack, ItemStack stackData) {}
}
