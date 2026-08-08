package fun.spmc.smpmod.fishing;

import net.minecraft.ChatFormatting;

public enum FishRarity {
    COMMON("Common", ChatFormatting.WHITE),
    UNCOMMON("Uncommon", ChatFormatting.GREEN),
    RARE("Rare", ChatFormatting.BLUE),
    EPIC("Epic", ChatFormatting.LIGHT_PURPLE),
    LEGENDARY("Legendary", ChatFormatting.GOLD),
    MYTHIC("Mythic", ChatFormatting.LIGHT_PURPLE),
    CHROMATIC("Chromatic", ChatFormatting.RED),
    CELESTIAL("Celestial", ChatFormatting.AQUA);

    private final String displayName;
    private final ChatFormatting color;

    FishRarity(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public ChatFormatting getColor() { return color; }
}
