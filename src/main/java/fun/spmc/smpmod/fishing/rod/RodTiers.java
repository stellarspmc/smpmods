package fun.spmc.smpmod.fishing.rod;

import net.minecraft.ChatFormatting;

public enum RodTiers {
    NORMAL(ChatFormatting.GRAY, 75, 1, .2f, new double[]{78, 18, 3.5, .45, .045, .004, .0008, .0002}, 1),
    COPPER(ChatFormatting.DARK_RED, 149, 1.1f, .22f, new double[]{68, 23, 7.5, 1.2, .25, .04, .008, .002}, 1),
    IRON(ChatFormatting.WHITE, 270, 1.15f, .25f, new double[]{56, 27, 12, 3.8, .9, .2, .08, .02}, 3),
    GOLD(ChatFormatting.GOLD, 113, 1.6f, .35f, new double[]{44, 30, 17, 6.5, 2, .4, .08, .02}, 8),
    EMERALD(ChatFormatting.GREEN, 364, 1.3f, .3f, new double[]{34, 31, 21, 10, 3.2, .65, .12, .03}, 5),
    DIAMOND(ChatFormatting.AQUA, 1012, 1.45f, .33f, new double[]{25, 32, 25, 12.5, 4.2, .95, .3, .05}, 5),
    NETHERITE(ChatFormatting.DARK_PURPLE, 2373, 1.8f, .35f, new double[]{18, 28, 31, 15, 5, 2, .8, .2}, 6);

    private final ChatFormatting color;
    private final float catchLuckBonus;
    private final float greenZoneSize;
    private final int durability;
    private final double[] rates; // 8 terms EXACT
    private final int lureSpeed; // reduction, in seconds

    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    public ChatFormatting getColor() { return color; }
    public int getDurability() { return durability; }
    public float getCatchLuckBonus() { return catchLuckBonus; }
    public float getGreenZoneSize() { return greenZoneSize; }
    public double[] getRates() { return rates; }
    public int getLureSpeed() { return lureSpeed; }

    RodTiers(ChatFormatting color, int durability, float catchLuckBonus, float greenZoneSize, double[] rates, int lureSpeed) {
        this.color = color;
        this.catchLuckBonus = catchLuckBonus;
        this.greenZoneSize = greenZoneSize;
        this.durability = durability;
        this.rates = rates;
        this.lureSpeed = lureSpeed;
    }
}
