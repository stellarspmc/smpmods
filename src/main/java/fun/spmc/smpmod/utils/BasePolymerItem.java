package fun.spmc.smpmod.utils;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class BasePolymerItem extends Item implements PolymerItem {
    private final Item vanillaItem;

    public BasePolymerItem(Properties properties, Item vanillaItem) {
        super(properties);
        this.vanillaItem = vanillaItem;
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
        out.set(DataComponents.CUSTOM_NAME, buildName(stack));
        out.set(DataComponents.LORE, new ItemLore(buildLore(stack)));
        modifyItem(out, stack);
    }

    public abstract Component buildName(ItemStack stack);
    public abstract List<Component> buildLore(ItemStack stack);
    public abstract void modifyItem(ItemStack stack, ItemStack stackData);
}

