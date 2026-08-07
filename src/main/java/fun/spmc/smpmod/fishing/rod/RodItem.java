package fun.spmc.smpmod.fishing.rod;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class RodItem extends SimplePolymerItem {
    private final RodTiers tier;

    public RodItem(Properties settings, RodTiers tier) {
        super(settings, Items.FISHING_ROD);
        this.tier = tier;
    }

    public RodTiers getTier() {
        return tier;
    }

    public ItemStack createStack() {
        ItemStack stack = new ItemStack(this);

        CompoundTag tag = new CompoundTag();
        tag.putString("rod_tier", tier.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.ITEM_NAME,
                Component.literal(tier.getName() + " Fishing Rod").withStyle(tier.getColor()));

        List<Component> loreLines = new ArrayList<>();
        loreLines.add(Component.literal("Tier: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(tier.getName()).withStyle(tier.getColor())));
        loreLines.add(Component.literal(String.format("Luck Bonus: +%.0f%%", (tier.getCatchLuckBonus() - 1.0f) * 100))
                .withStyle(ChatFormatting.GREEN));
        loreLines.add(Component.literal(String.format("Easy Reel Zone: %.0f%%", tier.getGreenZoneSize() * 100))
                .withStyle(ChatFormatting.AQUA));
        loreLines.add(Component.empty());
        loreLines.add(Component.literal("Use in water to start fishing minigame!").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        stack.set(DataComponents.LORE, new ItemLore(loreLines));

        return stack;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        super.modifyBasePolymerItemStack(out, stack, context, lookup);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!customData.isEmpty()) out.set(DataComponents.CUSTOM_DATA, customData);

        Component name = stack.get(DataComponents.ITEM_NAME);
        if (name != null) out.set(DataComponents.ITEM_NAME, name);

        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) out.set(DataComponents.LORE, lore);
    }
}