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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractCropItem extends SimplePolymerItem {
    private final Item vanillaItem;
    private final String seedItemName;

    public AbstractCropItem(Properties settings, Item item, String seedName) {
        super(settings);
        vanillaItem = item;
        seedItemName = seedName;
    }

    @Override public Item getPolymerItem(ItemStack itemStack, PacketContext context) { return vanillaItem; }
    public String getSeedName() { return seedItemName; }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        CustomData customData = out.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag rootTag = customData.copyTag();
        Optional<CompoundTag> cropTag = rootTag.getCompound("plant");
        if (cropTag.isPresent()) {
            out.set(DataComponents.ITEM_NAME, buildDynamicName(cropTag.get()));
            out.set(DataComponents.LORE, new ItemLore(buildDynamicLore(cropTag.get())));
        }
    }

    private Component buildDynamicName(CompoundTag cropTag) {
        List<PlantModifier> traits = parseTraits(cropTag);

        if (traits.isEmpty()) {
            return Component.literal(this.seedItemName).withStyle(ChatFormatting.WHITE);
        }

        MutableComponent title = Component.empty();
        for (PlantModifier trait : traits) {
            title.append(Component.literal(trait.toString() + " ").withStyle(trait.getColor()));
        }

        title.append(Component.literal(this.seedItemName).withStyle(ChatFormatting.GOLD));
        return title;
    }

    /**
     * Builds multi-line lore detailing quality, active traits, and market value bonuses
     */
    private List<Component> buildDynamicLore(CompoundTag cropTag) {
        List<Component> lore = new ArrayList<>();
        List<PlantModifier> traits = parseTraits(cropTag);
        if (cropTag.getInt("quality").isEmpty()) return List.of(Component.literal(""));
        int quality = cropTag.getInt("quality").get();

        String stars = "★".repeat(Math.clamp(quality, 1, 5));
        lore.add(Component.literal("Quality: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(stars).withStyle(ChatFormatting.YELLOW)));

        lore.add(Component.literal("")); // Empty spacer

        if (!traits.isEmpty()) {
            lore.add(Component.literal("Crop Traits:").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));

            int totalValueModifier = 0;
            for (PlantModifier trait : traits) {
                //totalValueModifier += trait.getValueModifier();

                Component traitLine = Component.literal(" • ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(trait.toString()).withStyle(trait.getColor()));
                        //.append(Component.literal(" (" + trait.getEffectDescription() + ")").withStyle(ChatFormatting.GRAY));

                lore.add(traitLine);
            }

            lore.add(Component.literal("")); // Empty spacer

            ChatFormatting valueColor = totalValueModifier >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            String prefix = totalValueModifier >= 0 ? "+" : "";
            lore.add(Component.literal("Sell Multiplier: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(prefix + totalValueModifier + "%").withStyle(valueColor)));
        } else {
            lore.add(Component.literal("• No special traits attached").withStyle(ChatFormatting.DARK_GRAY));
        }

        return lore;
    }

    private List<PlantModifier> parseTraits(CompoundTag cropTag) {
        List<PlantModifier> traits = new ArrayList<>();
        if (cropTag.contains("traits")) {
            //ListTag traitsList = cropTag.getList("traits", 8); // 8 = StringTag
            //for (int i = 0; i < traitsList.size(); i++) {
                //PlantModifier trait = PlantModifier.fromString(traitsList.getString(i));
                //if (trait != null) {
                    //traits.add(trait);
                //}
            //}
        }
        return traits;
    }

    public ItemStack createCropInstance(int quality, List<String> traitIds) {
        ItemStack stack = new ItemStack(this);

        CompoundTag rootTag = new CompoundTag();
        CompoundTag cropData = new CompoundTag();

        cropData.putString("id", seedItemName.toLowerCase().replace(" ", "_"));
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
