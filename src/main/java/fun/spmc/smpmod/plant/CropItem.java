package fun.spmc.smpmod.plant;

import fun.spmc.smpmod.misc.ItemModifier;
import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.utils.BasePolymerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.*;

public class CropItem extends BasePolymerItem {
    private final String cropName;
    private final double basePrice;
    private final ItemRarity rarity;

    public CropItem(Properties settings, Item vanillaItem, String cropName, double basePrice, ItemRarity rarity) {
        super(settings, vanillaItem);
        this.cropName = cropName;
        this.basePrice = basePrice;
        this.rarity = rarity;
    }

    public String getCropName() { return cropName; }
    public double getBasePrice() { return basePrice; }
    public ItemRarity getRarity() { return rarity; }

    @Override
    public Component buildName(ItemStack stack) {
        int quality = getQuality(getCropTag(stack));
        Set<ItemModifier> traits = getModifiers(getCropTag(stack)).keySet();
        MutableComponent title = Component.empty();
        for (ItemModifier trait : traits) title.append(Component.literal(trait.toString() + " ").withColor(trait.getColor()));
        title.append(Component.literal(this.cropName).withStyle(rarity.getColor()));
        if (quality != 0) title.append(Component.literal(" " + (quality > 0 ? "★".repeat(quality) : "\uD83D\uDC80".repeat(-quality))).withStyle(ChatFormatting.YELLOW));
        return title.withStyle(style -> style.withItalic(false));
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("Value: $" + getModifiedPrice(stack)).withStyle(ChatFormatting.GOLD).withStyle(style -> style.withItalic(false)));
        return lore;
    }

    @Override
    public void modifyItem(ItemStack out, ItemStack stackData) {
        CompoundTag tag = getCropTag(stackData);
        int quality = getQuality(tag);
        if (quality >= 4 || quality <= 0) out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
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

    public static CompoundTag getCropTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getCompound("crop")
                .orElse(new CompoundTag());
    }

    public static int getQuality(CompoundTag cropTag) {
        return cropTag.getIntOr("quality", 1);
    }

    private static Map<ItemModifier, Integer> getModifiers(CompoundTag tag) {
        Map<ItemModifier, Integer> map = new HashMap<>();
        if (tag.getCompound("modifier").isPresent()) {
            CompoundTag modTag = tag.getCompound("modifier").get();
            modTag.forEach((id, level) -> ItemModifier.fromId(id).ifPresent(mod -> level.asInt().ifPresent(lvl -> map.put(mod, lvl))));
        }
        return map;
    }

    public double getModifiedPrice(ItemStack stack) {
        CompoundTag tag = getCropTag(stack);
        int quality = getQuality(tag);
        double price = this.basePrice * stack.getCount();
        return Math.round(price * (1 + (quality * .45)) * 100.0) / 100.0;
    }
}
