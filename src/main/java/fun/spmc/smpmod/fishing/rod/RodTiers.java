package fun.spmc.smpmod.fishing.rod;

import net.minecraft.ChatFormatting;

public enum RodTiers {
    NORMAL("Normal", ChatFormatting.GRAY, 75, 1, .2f, {}, 1),
    COPPER("Copper", ChatFormatting.DARK_RED, 149, 1.1f, .22f, {}, 1),
    IRON("Iron", ChatFormatting.WHITE, 270, 1.15f, .25f, {}, 3),
    GOLD("Gold", ChatFormatting.GOLD, 113, 1.6f, .35f, {}, 8),
    EMERALD("Emerald", ChatFormatting.GREEN, 364, 1.3f, .3f, {}, 5),
    DIAMOND("Diamond", ChatFormatting.AQUA, 1012, 1.45f, .33f, {}, 5),
    NETHERITE("Netherite", ChatFormatting.DARK_PURPLE, 2373, 1.8f, .35f, {}, 6);

    private final String name;
    private final ChatFormatting color;
    private final float catchLuckBonus;
    private final float greenZoneSize;
    private final int durability;
    private final double[] rates; // 8 terms
    private final int lureSpeed; // reduction, in seconds

    public String getName() { return name; }
    public ChatFormatting getColor() { return color; }
    public int getDurability() { return durability; }
    public float getCatchLuckBonus() { return catchLuckBonus; }
    public float getGreenZoneSize() { return greenZoneSize; }
    public double[] getRates() { return rates; }
    public int getLureSpeed() { return lureSpeed; }

    RodTiers(String name, ChatFormatting color, float catchLuckBonus, float greenZoneSize, int durability, double[] rates, int lureSpeed) {
        this.name = name;
        this.color = color;
        this.catchLuckBonus = catchLuckBonus;
        this.greenZoneSize = greenZoneSize;
        this.durability = durability;
        this.rates = rates;
        this.lureSpeed = lureSpeed;
    }
}
