package fun.spmc.smpmod.misc;

import net.minecraft.ChatFormatting;

public enum ItemRarity {
    COMMON(ChatFormatting.WHITE, false),
    UNCOMMON(ChatFormatting.GREEN, false),
    RARE(ChatFormatting.BLUE, false),
    EPIC(ChatFormatting.DARK_PURPLE, false),
    LEGENDARY(ChatFormatting.GOLD, true),
    MYTHIC(ChatFormatting.LIGHT_PURPLE, true),
    CHROMATIC(ChatFormatting.RED, true),
    CELESTIAL(ChatFormatting.AQUA, true);

    private final ChatFormatting color;
    private final boolean consideredRare;

    ItemRarity(ChatFormatting color, boolean consideredRare) { this.color = color; this.consideredRare = consideredRare; }
    public ChatFormatting getColor() { return color; }
    public boolean shouldAnnounce() { return consideredRare; }
    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
}
