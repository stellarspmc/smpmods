package fun.spmc.smpmod.fishing;

import fun.spmc.smpmod.misc.ItemModifier;
import net.minecraft.network.chat.TextColor;

public enum RodTiers {
    NORMAL(TextColor.fromRgb(0x706E6B), 34, 1f, .23f, new double[]{78, 18, 3.5, .45, .045, .004, .0008, .0002}, 1, new ItemModifier[]{}),
    RAINBOW(TextColor.fromRgb(0xFF70A6), 102, 1.15f, .2f, new double[]{70, 20, 8, 1.6, .35, .04, .008, .002}, 1, new ItemModifier[]{ItemModifier.COLORFUL}),
    COPPER(TextColor.fromRgb(0xE07A5F), 89, 1.1f, .22f, new double[]{68, 23, 7.5, 1.2, .25, .04, .008, .002}, 1, new ItemModifier[]{}),
    IRON(TextColor.fromRgb(0xD0D7DC), 177, 1.15f, .25f, new double[]{56, 27, 12, 3.8, .9, .2, .08, .02}, 3, new ItemModifier[]{}),
    GOLD(TextColor.fromRgb(0xFFD700), 48, 1.6f, .33f, new double[]{44, 30, 17, 6.5, 2, .4, .08, .02}, 8, new ItemModifier[]{ItemModifier.GOLDEN}),
    EMERALD(TextColor.fromRgb(0x2ECC71), 259, 1.3f, .3f, new double[]{34, 31, 21, 10, 3.2, .65, .12, .03}, 5, new ItemModifier[]{}),
    LUNA(TextColor.fromRgb(0x9B59B6), 152, 1.65f, .35f, new double[]{28, 42, 21, 8, .8, .19, .0075, .0025}, 6, new ItemModifier[]{}),
    DIAMOND(TextColor.fromRgb(0x3498DB), 533, 1.45f, .33f, new double[]{25, 32, 25, 12.5, 4.2, .95, .3, .05}, 5, new ItemModifier[]{ItemModifier.CRYSTALLIZED}),
    NETHERITE(TextColor.fromRgb(0x4A3B4E), 1007, 1.8f, .35f, new double[]{18, 28, 31, 15, 5, 2, .8, .2}, 6, new ItemModifier[]{}),
    TOXIC(TextColor.fromRgb(0x39FF14), 850, 1.75f, .32f, new double[]{20, 28, 30, 14, 5.3, 2, .5, .2}, 6, new ItemModifier[]{ItemModifier.NUCLEAR}),
    DEATH(TextColor.fromRgb(0x800020), 666, 1.9f, .28f, new double[]{15, 25, 32, 16, 8, 2.8, 1.0, .2}, 7, new ItemModifier[]{ItemModifier.EVIL}),
    AIR(TextColor.fromRgb(0xA0E7E5), 751, 1.7f, .3f, new double[]{22, 30, 28, 12, 5, 2.1, .7, .2}, 10, new ItemModifier[]{ItemModifier.SPEEDY}),
    SEA(TextColor.fromRgb(0x0077B6), 717, 1.85f, .36f, new double[]{16, 26, 32, 15.5, 6, 2.5, 1.2, .2}, 7, new ItemModifier[]{ItemModifier.BRUCED}),
    FLICKERING(TextColor.fromRgb(0xFFD166), 549, 2f, .3f, new double[]{14, 24, 30, 18, 8, 3.5, 2.0, .5}, 8, new ItemModifier[]{ItemModifier.GOLDEN}),
    CELESTIAL(TextColor.fromRgb(0x9D4EDD), 1211, 2.2f, .37f, new double[]{10, 20, 32, 20, 11, 4.5, 2.0, .5}, 9, new ItemModifier[]{}),
    ELEMENTAL(TextColor.fromRgb(0xFF5722), 1496, 2.6f, .48f, new double[]{6, 14, 30, 24, 15, 7, 3.2, .8}, 12, new ItemModifier[]{ItemModifier.GODLY}),
    CTHULHU(TextColor.fromRgb(0x00F5D4), 1574, 2.7f, .48f, new double[]{5, 13, 28, 25, 16, 8, 4, 1}, 14, new ItemModifier[]{ItemModifier.SPEEDY}),
    EVERYTHING(TextColor.fromRgb(0xE056FD), 1689, 3.2f, .4f, new double[]{2, 8.9, 21.8, 28, 22, 12, 4.2, 1.1}, 16, new ItemModifier[]{ItemModifier.GODLY, ItemModifier.ULTIMATE});

    private final TextColor color;
    private final float catchLuckBonus;
    private final float greenZoneSize;
    private final int durability;
    private final double[] rates; // 8 terms EXACT
    private final int lureSpeed; // reduction, in seconds
    private final ItemModifier[] obtainable;

    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    public TextColor getColor() { return color; }
    public int getDurability() { return durability; }
    public float getCatchLuckBonus() { return catchLuckBonus; }
    public float getGreenZoneSize() { return greenZoneSize; }
    public double[] getRates() { return rates; }
    public int getLureSpeed() { return lureSpeed; }
    public ItemModifier[] getObtainable() { return obtainable; }
    RodTiers(TextColor color, int durability, float catchLuckBonus, float greenZoneSize, double[] rates, int lureSpeed, ItemModifier[] obtainable) {
        this.color = color;
        this.catchLuckBonus = catchLuckBonus;
        this.greenZoneSize = greenZoneSize;
        this.durability = durability;
        this.rates = rates;
        this.lureSpeed = lureSpeed;
        this.obtainable = obtainable;
    }
}
