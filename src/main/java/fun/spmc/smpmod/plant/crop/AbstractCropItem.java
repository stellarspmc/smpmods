package fun.spmc.smpmod.plant.crop;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import fun.spmc.smpmod.plant.PlantModifier;
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

public abstract class AbstractCropItem extends SimplePolymerItem {
    private final Item vanillaItem;
    private final String cropName;
    private final double basePrice;

    public AbstractCropItem(Properties settings, Item item, String seedName, double basePrice) {
        super(settings);
        vanillaItem = item;
        cropName = seedName;
        this.basePrice = basePrice;
    }

    @Override public Item getPolymerItem(ItemStack itemStack, PacketContext context) { return vanillaItem; }
    public String getCropName() { return cropName; }
    public double getBasePrice() { return basePrice; }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        CustomData customData = out.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag rootTag = customData.copyTag();
        Optional<CompoundTag> cropTag = rootTag.getCompound("plant");
        if (cropTag.isPresent()) {
            out.set(DataComponents.ITEM_NAME, buildName(cropTag.get()));
            out.set(DataComponents.LORE, new ItemLore(buildLore(cropTag.get())));
        }
    }

    private Component buildName(CompoundTag cropTag) {
        Set<PlantModifier> traits = getModifiers(cropTag).keySet();
        if (traits.isEmpty()) return Component.literal(this.cropName).withStyle(ChatFormatting.WHITE);
        MutableComponent title = Component.empty();
        for (PlantModifier trait : traits) title.append(Component.literal(trait.toString() + " ").withStyle(trait.getColor()));
        title.append(Component.literal(this.cropName).withStyle(ChatFormatting.GOLD));
        return title;
    }

    private List<Component> buildLore(CompoundTag cropTag) {
        return List.of();
    }

    private HashMap<PlantModifier, Integer> getModifiers(CompoundTag tag) {
        HashMap<PlantModifier, Integer> hash = new HashMap<>();
        if (tag.getCompound("modifier").isPresent()) {
            CompoundTag modTag = tag.getCompound("modifier").get();
            modTag.forEach((id, level) -> {
                List<PlantModifier> mods = Arrays.stream(PlantModifier.values()).filter((plant) -> id.equals(plant.name().toLowerCase())).toList();
                if (mods.size() == 1) if (level.asInt().isPresent()) hash.put(mods.getFirst(), level.asInt().get());
            });
        }
        return hash;
    }

    private int getQuality(CompoundTag tag) { return tag.getIntOr("quality", 0); }

    public ItemStack createCropInstance(int quality, List<String> traitIds) {
        ItemStack stack = new ItemStack(this);

        CompoundTag rootTag = new CompoundTag();
        CompoundTag cropData = new CompoundTag();

        cropData.putString("id", cropName.toLowerCase().replace(" ", "_"));
        cropData.putInt("quality", quality);

        ListTag traitsList = new ListTag();
        for (String traitId : traitIds) {
            traitsList.add(StringTag.valueOf(traitId));
        }
        cropData.put("traits", traitsList);
        rootTag.put("plant", cropData);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("plant", cropData));

        return stack;
    }
}
