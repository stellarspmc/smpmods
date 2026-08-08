package fun.spmc.smpmod.fishing.rod;

import net.minecraft.ChatFormatting;

public enum RodTiers {
    NORMAL("Normal", ChatFormatting.GRAY, 75, 1, .2f),
    COPPER("Copper", ChatFormatting.DARK_RED, 149, 1.1f, .25f),
    IRON("Iron", ChatFormatting.WHITE, 270, 1.2f, .3f),
    GOLD("Gold", ChatFormatting.GOLD, 64, 1.3f, .35f),
    EMERALD("Emerald", ChatFormatting.GREEN, 364, 1.4f, .4f),
    DIAMOND("Diamond", ChatFormatting.AQUA, 1012, 1.5f, .45f),
    NETHERITE("Netherite", ChatFormatting.DARK_PURPLE, 2373, 1.8f, .5f);

    private final String name;
    private final ChatFormatting color;
    private final float catchLuckBonus;
    private final float greenZoneSize; // Percentage of the minigame bar that is "green"
    private final int durability;

    RodTiers(String name, ChatFormatting color, int durability, float catchLuckBonus, float greenZoneSize) {
        this.name = name;
        this.color = color;
        this.durability = durability;
        this.catchLuckBonus = catchLuckBonus;
        this.greenZoneSize = greenZoneSize;
    }

    public String getName() { return name; }
    public ChatFormatting getColor() { return color; }
    public int getDurability() { return durability; }
    public float getCatchLuckBonus() { return catchLuckBonus; }
    public float getGreenZoneSize() { return greenZoneSize; }
}
