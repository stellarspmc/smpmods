package fun.spmc.smpmod.fishing.fish;

import fun.spmc.smpmod.fishing.FishModifier;
import fun.spmc.smpmod.fishing.FishRarity;
import fun.spmc.smpmod.utils.SimplerPolymerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.*;

public class FishItem extends SimplerPolymerItem {
    private final String fishName;
    private final double basePrice;
    private final FishRarity rarity;

    public FishItem(Properties settings, Item vanillaItem, String fishName, double basePrice, FishRarity rarity) {
        super(settings.stacksTo(64), vanillaItem);
        this.fishName = fishName;
        this.basePrice = basePrice;
        this.rarity = rarity;
    }

    public FishRarity getRarity() { return rarity; }
    public String getFishName() { return fishName; }
    public double getBasePrice() { return basePrice; }

    @Override
    public Component buildName(ItemStack stack) {
        Optional<CompoundTag> fishTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("fish");
        if (fishTag.isPresent()) {
            CompoundTag tag = fishTag.get();
            Set<FishModifier> traits = getModifiers(tag).keySet();
            int quality = getQuality(tag);

            MutableComponent title = Component.empty();
            for (FishModifier trait : traits) title.append(Component.literal(trait.toString() + " ").withStyle(trait.getColor()));
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
        if (fishTag.isPresent()) {
            CompoundTag tag = fishTag.get();
            int quality = getQuality(tag);

            double multiplier = 1.0 + (quality * 0.25);
            Map<FishModifier, Integer> modifiers = getModifiers(tag);
            for (FishModifier mod : modifiers.keySet()) multiplier += mod.getPriceMultiplier();
            double finalPrice = Math.round((basePrice * multiplier) * 100.0) / 100.0;

            lore.add(Component.literal("Base Price: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("$" + finalPrice).withStyle(ChatFormatting.GREEN)));

            if (!modifiers.isEmpty()) {
                lore.add(Component.empty());
                lore.add(Component.literal("Traits:").withStyle(ChatFormatting.GRAY));
                for (Map.Entry<FishModifier, Integer> entry : modifiers.entrySet()) {
                    lore.add(Component.literal(" • " + entry.getKey().toString()).withStyle(entry.getKey().getColor()));
                }
            }
        }
        return lore;
    }

    @Override
    public void modifyItem(ItemStack stack) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private Map<FishModifier, Integer> getModifiers(CompoundTag tag) {
        Map<FishModifier, Integer> map = new HashMap<>();
        if (tag.getCompound("modifier").isPresent()) {
            CompoundTag modTag = tag.getCompound("modifier").get();
            modTag.forEach((id, level) -> FishModifier.fromId(id).ifPresent(mod -> level.asInt().ifPresent(lvl -> map.put(mod, lvl))));
        }
        return map;
    }

    private int getQuality(CompoundTag tag) {
        return tag.getIntOr("quality", 0);
    }

    public ItemStack createFishInstance(int quality, List<FishModifier> traits) {
        ItemStack stack = new ItemStack(this);

        CompoundTag fishData = new CompoundTag();
        fishData.putString("id", fishName.toLowerCase().replace(" ", "_"));
        fishData.putInt("quality", quality);

        CompoundTag modTag = new CompoundTag();
        for (FishModifier trait : traits) modTag.putInt(trait.name().toLowerCase(), 1);
        fishData.put("modifier", modTag);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("fish", fishData));

        return stack;
    }
}