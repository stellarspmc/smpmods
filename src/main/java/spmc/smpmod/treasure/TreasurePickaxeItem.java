package spmc.smpmod.treasure;

import spmc.smpmod.utils.BasePolymerItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class TreasurePickaxeItem extends BasePolymerItem {
    private final PickaxeTiers tier;
    private final Item vanillaItem;

    public TreasurePickaxeItem(Properties properties, Item vanillaItem, PickaxeTiers tier) {
        super(properties, vanillaItem);
        this.tier = tier;
        this.vanillaItem = vanillaItem;
    }

    @Override
    public Component buildName(ItemStack stack) {
        return null;
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        return List.<Component>of();
    }

    @Override
    public void modifyItem(ItemStack stack, ItemStack stackData) {

    }
}
