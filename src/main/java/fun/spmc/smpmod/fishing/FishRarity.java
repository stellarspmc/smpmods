package fun.spmc.smpmod.fishing;

import net.minecraft.ChatFormatting;

public enum FishRarity {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.GREEN),
    RARE(ChatFormatting.BLUE),
    EPIC(ChatFormatting.DARK_PURPLE),
    LEGENDARY(ChatFormatting.GOLD),
    MYTHIC(ChatFormatting.LIGHT_PURPLE),
    CHROMATIC(ChatFormatting.RED),
    CELESTIAL(ChatFormatting.AQUA);

    private final ChatFormatting color;

    FishRarity(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting getColor() { return color; }
}
