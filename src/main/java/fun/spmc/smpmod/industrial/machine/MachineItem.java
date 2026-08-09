package fun.spmc.smpmod.industrial.machine;

import fun.spmc.smpmod.utils.BasePolymerBlockItem;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class MachineItem extends BasePolymerBlockItem {
    private final String id;

    public MachineItem(Block block, Properties properties, Item vanillaItem, String id) {
        super(block, properties, vanillaItem);
        this.id = id;
    }

    @Override
    public Component buildName(ItemStack stack) {
        return Component.literal(MessageUtils.formatName(id));
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
