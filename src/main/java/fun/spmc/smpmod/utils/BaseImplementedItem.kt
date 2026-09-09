package fun.spmc.smpmod.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BaseImplementedItem extends BasePolymerBlockItem {
    private final String id;

    public BaseImplementedItem(Block block, Properties properties, Item vanillaItem, String id) {
        super(block, properties, vanillaItem);
        this.id = id;
    }

    @Override
    public Component buildName(ItemStack stack) {
        return Component.literal(MessageUtils.formatName(id)).withStyle(style -> style.withItalic(false));
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        return List.of();
    }

    @Override
    public void modifyItem(ItemStack stack, ItemStack stackData) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stackData.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
}
