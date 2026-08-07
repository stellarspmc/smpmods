package fun.spmc.smpmod.fishing.rod;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

public enum RodTiers {
    NORMAL("Normal", ChatFormatting.GRAY, 75, 1.0f, 0.20f, Items.FISHING_ROD),
    COPPER("Copper", ChatFormatting.DARK_RED, 149, 1.1f, 0.25f, Items.COPPER_INGOT),
    IRON("Iron", ChatFormatting.WHITE, 270, 1.2f, 0.30f, Items.IRON_INGOT),
    GOLD("Gold", ChatFormatting.GOLD, 64, 1.3f, 0.35f, Items.GOLD_INGOT),
    EMERALD("Emerald", ChatFormatting.GREEN, 364, 1.4f, 0.40f, Items.EMERALD),
    DIAMOND("Diamond", ChatFormatting.AQUA, 1012, 1.5f, 0.45f, Items.DIAMOND),
    NETHERITE("Netherite", ChatFormatting.DARK_PURPLE, 2373, 1.8f, 0.50f, Items.NETHERITE_INGOT);

    private final String name;
    private final ChatFormatting color;
    private final float catchLuckBonus;
    private final float greenZoneSize; // Percentage of the minigame bar that is "green"
    private final Item craftIngredient;
    private final int durability;

    RodTiers(String name, ChatFormatting color, int durability, float catchLuckBonus, float greenZoneSize, Item craftIngredient) {
        this.name = name;
        this.color = color;
        this.durability = durability;
        this.catchLuckBonus = catchLuckBonus;
        this.greenZoneSize = greenZoneSize;
        this.craftIngredient = craftIngredient;
    }

    public String getName() { return name; }
    public ChatFormatting getColor() { return color; }
    public int getDurability() { return durability; }
    public float getCatchLuckBonus() { return catchLuckBonus; }
    public float getGreenZoneSize() { return greenZoneSize; }
    public Item getCraftIngredient() { return craftIngredient; }
}
