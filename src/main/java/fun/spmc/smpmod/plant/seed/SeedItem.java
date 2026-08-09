package fun.spmc.smpmod.plant.seed;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SeedItem extends PolymerBlockItem {
    private final Item vanillaItem;
    private final String seedName;

    public SeedItem(Block block, Properties settings, Item vanillaItem, String seedName) {
        super(block, settings);
        this.vanillaItem = vanillaItem;
        this.seedName = seedName;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return vanillaItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return BuiltInRegistries.ITEM.getKey(vanillaItem);
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        out.set(DataComponents.CUSTOM_NAME, Component.literal(seedName).withStyle(ChatFormatting.GREEN));
        out.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Plant on Farmland to grow " + seedName.replace(" Seeds", "")).withStyle(ChatFormatting.GRAY)
        )));
    }
}