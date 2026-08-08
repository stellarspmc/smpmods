package fun.spmc.smpmod.plant.crop;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import fun.spmc.smpmod.plant.PlantModifier;
import fun.spmc.smpmod.utils.SimplerPolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.*;

public class CropItem extends SimplerPolymerItem {
    private final String cropName;
    private final double basePrice;

    public CropItem(Properties settings, Item item, String seedName, double basePrice) {
        super(settings, item);
        cropName = seedName;
        this.basePrice = basePrice;
    }

    public String getCropName() { return cropName; }

    public double getBasePrice() { return basePrice; }

    @Override
    public Component buildName(ItemStack stack) {
        return null;
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        return List.of();
    }

    @Override
    public void modifyItem(ItemStack stack, ItemStack stackData) {

    }
}
