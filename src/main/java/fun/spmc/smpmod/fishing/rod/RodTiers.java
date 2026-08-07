package fun.spmc.smpmod.fishing.rod;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

public enum RodTiers {
    NORMAL("Normal", ChatFormatting.GRAY, 100, 1, 1.0f, 0.20f, Items.FISHING_ROD),
    COPPER("Copper", ChatFormatting.DARK_RED, 250, 5, 1.1f, 0.25f, Items.COPPER_INGOT),
    IRON("Iron", ChatFormatting.WHITE, 1000, 25, 1.2f, 0.30f, Items.IRON_INGOT),
    GOLD("Gold", ChatFormatting.GOLD, 5000, 100, 1.3f, 0.35f, Items.GOLD_INGOT),
    EMERALD("Emerald", ChatFormatting.GREEN, 10000, 250, 1.4f, 0.40f, Items.EMERALD),
    DIAMOND("Diamond", ChatFormatting.AQUA, 25000, 500, 1.5f, 0.45f, Items.DIAMOND),
    NETHERITE("Netherite", ChatFormatting.DARK_PURPLE, 100000, 2500, 1.8f, 0.50f, Items.NETHERITE_INGOT);

    private final String name;
    private final ChatFormatting color;
    private final double buyPriceMoney;
    private final int getBuyPriceFishies;
    private final float catchLuckBonus;
    private final float greenZoneSize; // Percentage of the minigame bar that is "green"
    private final Item craftIngredient;

    RodTiers(String name, ChatFormatting color, double buyPriceMoney, int getBuyPriceFishies, float catchLuckBonus, float greenZoneSize, Item craftIngredient) {
        this.name = name;
        this.color = color;
        this.buyPriceMoney = buyPriceMoney;
        this.getBuyPriceFishies = getBuyPriceFishies;
        this.catchLuckBonus = catchLuckBonus;
        this.greenZoneSize = greenZoneSize;
        this.craftIngredient = craftIngredient;
    }

    public String getName() { return name; }
    public ChatFormatting getColor() { return color; }
    public double getBuyPriceMoney() { return buyPriceMoney; }
    public int getBuyPriceFishies() { return getBuyPriceFishies; }
    public float getCatchLuckBonus() { return catchLuckBonus; }
    public float getGreenZoneSize() { return greenZoneSize; }
    public Item getCraftIngredient() { return craftIngredient; }
}
