package fun.spmc.smpmod.plant.crop;

import fun.spmc.smpmod.fishing.fish.FishItem;
import fun.spmc.smpmod.misc.ItemModifier;
import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.utils.SimplerPolymerItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.*;

public class CropItem extends SimplerPolymerItem {
    private final String cropName;
    private final double basePrice;
    private final ItemRarity rarity;

    public CropItem(Properties settings, Item item, String seedName, double basePrice, ItemRarity rarity) {
        super(settings, item);
        cropName = seedName;
        this.basePrice = basePrice;
        this.rarity = rarity;
    }

    public String getCropName() { return cropName; }
    public double getBasePrice() { return basePrice; }
    public ItemRarity getRarity() { return rarity; }

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
        Optional<CompoundTag> tag = stackData.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("plant");
        if (tag.isPresent()) {
            int quality = getQuality(tag.get());
            if (quality > 3) {
                stackData.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }
    }

    private static Map<ItemModifier, Integer> getModifiers(CompoundTag tag) {
        Map<ItemModifier, Integer> map = new HashMap<>();
        if (tag.getCompound("modifier").isPresent()) {
            CompoundTag modTag = tag.getCompound("modifier").get();
            modTag.forEach((id, level) -> ItemModifier.fromId(id).ifPresent(mod -> level.asInt().ifPresent(lvl -> map.put(mod, lvl))));
        }
        return map;
    }

    private static int getQuality(CompoundTag tag) {
        return tag.getIntOr("quality", 0);
    }

    public ItemStack createCropInstance(int quality, Map<ItemModifier, Integer> mods) {
        ItemStack stack = new ItemStack(this);

        CompoundTag cropTag = new CompoundTag();
        cropTag.putString("id", cropName.toLowerCase().replace(" ", "_"));
        cropTag.putInt("quality", quality);

        CompoundTag modTag = new CompoundTag();
        mods.forEach((mod, level) -> modTag.putInt(mod.name().toLowerCase(), level));
        cropTag.put("modifier", modTag);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("crop", cropTag));
        return stack;
    }

    public static double getModifiedPrice(ItemStack stack) {
        Optional<CompoundTag> cropTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("crop");
        if (cropTag.isPresent()) {
            CompoundTag tag = cropTag.get();
            int quality = getQuality(tag);
            Map<ItemModifier, Integer> modifiers = getModifiers(tag);
            double price = ((FishItem) (stack.getItem())).getBasePrice() * stack.getCount();
            for (Map.Entry<ItemModifier, Integer> entry : modifiers.entrySet()) price *= entry.getKey().getPriceMultiplier() * Math.min(1, entry.getValue());
            return Math.round(price * (quality * .4 + 1) * 100) / 100d;
        } return ((FishItem) (stack.getItem())).getBasePrice() * stack.getCount();
    }
}
