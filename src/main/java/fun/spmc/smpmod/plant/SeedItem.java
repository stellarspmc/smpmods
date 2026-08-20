package fun.spmc.smpmod.plant;

import fun.spmc.smpmod.utils.BasePolymerBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class SeedItem extends BasePolymerBlockItem {
    private final String seedName;

    public SeedItem(Block block, Properties settings, Item vanillaItem, String seedName) {
        super(block, settings, vanillaItem);
        this.seedName = seedName;
    }

    @Override
    public Component buildName(ItemStack stack) {
        return Component.literal(seedName).withStyle(ChatFormatting.GREEN).withStyle(style -> style.withItalic(false));
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        return List.of(Component.literal("Plant on Farmland to grow " + seedName.replace(" Seeds", "")).withStyle(ChatFormatting.GRAY).withStyle(style -> style.withItalic(false)));
    }

    @Override
    public void modifyItem(ItemStack stack, ItemStack stackData) {}
}