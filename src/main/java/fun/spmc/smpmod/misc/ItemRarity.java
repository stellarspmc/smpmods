package fun.spmc.smpmod.misc;

import net.minecraft.ChatFormatting;

public enum ItemRarity {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.GREEN),
    RARE(ChatFormatting.BLUE),
    EPIC(ChatFormatting.DARK_PURPLE),
    LEGENDARY(ChatFormatting.GOLD),
    MYTHIC(ChatFormatting.LIGHT_PURPLE),
    CHROMATIC(ChatFormatting.RED),
    CELESTIAL(ChatFormatting.AQUA);

    private final ChatFormatting color;

    ItemRarity(ChatFormatting color) { this.color = color; }
    public ChatFormatting getColor() { return color; }
    @Override public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
}
