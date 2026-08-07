package fun.spmc.smpmod.fishing.fish;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import fun.spmc.smpmod.fishing.FishModifier;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.*;

public class FishItem extends SimplePolymerItem {
    private final Item vanillaItem;
    private final String fishName;
    private final double basePrice;

    public FishItem(Properties settings, Item vanillaItem, String fishName, double basePrice) {
        super(settings);
        this.vanillaItem = vanillaItem;
        this.fishName = fishName;
        this.basePrice = basePrice;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return vanillaItem;
    }

    public String getFishName() { return fishName; }
    public double getBasePrice() { return basePrice; }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag rootTag = customData.copyTag();

        Optional<CompoundTag> fishTag = rootTag.getCompound("fish");
        if (fishTag.isPresent()) {
            CompoundTag tag = fishTag.get();
            out.set(DataComponents.ITEM_NAME, buildName(tag));
            out.set(DataComponents.LORE, new ItemLore(buildLore(tag)));
        }
    }

    private Component buildName(CompoundTag fishTag) {
        Set<FishModifier> traits = getModifiers(fishTag).keySet();
        int quality = getQuality(fishTag);

        MutableComponent title = Component.empty();

        for (FishModifier trait : traits) title.append(Component.literal(trait.toString() + " ").withStyle(trait.getColor()));
        title.append(Component.literal(this.fishName).withStyle(ChatFormatting.GOLD));
        if (quality > 0) title.append(Component.literal(" " + "★".repeat(quality)).withStyle(ChatFormatting.YELLOW));
        return title;
    }

    private List<Component> buildLore(CompoundTag fishTag) {
        List<Component> lore = new ArrayList<>();
        int quality = getQuality(fishTag);

        double multiplier = 1.0 + (quality * 0.25);
        Map<FishModifier, Integer> modifiers = getModifiers(fishTag);
        for (FishModifier mod : modifiers.keySet()) {
            multiplier += mod.getPriceMultiplier();
        }

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

        return lore;
    }

    private Map<FishModifier, Integer> getModifiers(CompoundTag tag) {
        Map<FishModifier, Integer> map = new HashMap<>();
        if (tag.getCompound("modifier").isPresent()) {
            CompoundTag modTag = tag.getCompound("modifier").get();
            modTag.forEach((id, level) -> {
                FishModifier.fromId(id).ifPresent(mod -> level.asInt().ifPresent(lvl -> map.put(mod, lvl)));
            });
        }
        return map;
    }

    private int getQuality(CompoundTag tag) {
        return tag.getIntOr("quality", 0);
    }

    public ItemStack createFishInstance(int quality, List<FishModifier> traits) {
        ItemStack stack = new ItemStack(this);

        CompoundTag cropData = new CompoundTag();
        cropData.putString("id", fishName.toLowerCase().replace(" ", "_"));
        cropData.putInt("quality", quality);

        CompoundTag modTag = new CompoundTag();
        for (FishModifier trait : traits) modTag.putInt(trait.name().toLowerCase(), 1);
        cropData.put("modifier", modTag);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("fish", cropData));

        return stack;
    }
}