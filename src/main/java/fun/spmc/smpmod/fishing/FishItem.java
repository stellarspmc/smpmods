package fun.spmc.smpmod.fishing;

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
import org.jspecify.annotations.NonNull;

import java.util.*;

public class FishItem extends BasePolymerItem {
    private final String fishName;
    private final double basePrice;
    private final ItemRarity rarity;

    public FishItem(Properties settings, Item vanillaItem, String fishName, double basePrice, ItemRarity rarity) {
        super(settings.stacksTo(64), vanillaItem);
        this.fishName = fishName;
        this.basePrice = basePrice;
        this.rarity = rarity;
    }

    public ItemRarity getRarity() { return rarity; }
    public String getFishName() { return fishName; }
    public double getBasePrice() { return basePrice; }

    @Override
    public void modifyItem(ItemStack stack, ItemStack stackData) {
        Optional<CompoundTag> fishTag = stackData.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish");
        if (fishTag.isPresent()) {
            int quality = getQuality(fishTag.get());
            if (quality > 3) {
                stackData.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack itemStack) {
        return buildName(itemStack);
    }

    @Override
    public Component buildName(ItemStack stack) {
        Optional<CompoundTag> fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish");
        if (fishTag.isPresent()) {
            CompoundTag tag = fishTag.get();
            Set<ItemModifier> traits = getModifiers(tag).keySet();
            int quality = getQuality(tag);

            MutableComponent title = Component.empty();
            for (ItemModifier trait : traits) title.append(Component.literal(trait.toString() + " ").withColor(trait.getColor()));
            title.append(Component.literal(this.fishName).withStyle(rarity.getColor()));
            if (quality > 0) title.append(Component.literal(" " + "★".repeat(quality)).withStyle(ChatFormatting.YELLOW));
            return title.withStyle(style -> style.withItalic(false));
        }
        return Component.literal(this.fishName).withStyle(rarity.getColor()).withStyle(style -> style.withItalic(false));
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        List<Component> lore = new ArrayList<>();
        Optional<CompoundTag> fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish");
        if (fishTag.isPresent()) lore.add(Component.literal("Price: ").withStyle(ChatFormatting.GRAY).append(Component.literal("$" + getModifiedPrice(stack)).withStyle(ChatFormatting.GREEN)).withStyle(style -> style.withItalic(false)));
        return lore;
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

    public ItemStack createFishInstance(int quality, Map<ItemModifier, Integer> mods) {
        ItemStack stack = new ItemStack(this);

        CompoundTag fishData = new CompoundTag();
        fishData.putString("id", fishName.toLowerCase().replace(" ", "_"));
        fishData.putInt("quality", quality);

        CompoundTag modTag = new CompoundTag();
        mods.forEach((mod, level) -> modTag.putInt(mod.name().toLowerCase(), level));
        fishData.put("modifier", modTag);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("fish", fishData));
        return stack;
    }

    public static double getModifiedPrice(ItemStack stack) {
        Optional<CompoundTag> fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish");
        if (fishTag.isPresent()) {
            CompoundTag tag = fishTag.get();
            int quality = getQuality(tag);
            Map<ItemModifier, Integer> modifiers = getModifiers(tag);
            double price = ((FishItem) (stack.getItem())).getBasePrice() * stack.getCount();
            for (Map.Entry<ItemModifier, Integer> entry : modifiers.entrySet()) price *= entry.getKey().getPriceMultiplier() * Math.min(1, entry.getValue());
            return Math.round(price * (quality * .15 + 1) * 100) / 100d;
        } return ((FishItem) (stack.getItem())).getBasePrice() * stack.getCount();
    }
}