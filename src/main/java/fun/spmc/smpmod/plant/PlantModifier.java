package fun.spmc.smpmod.plant;

import net.minecraft.ChatFormatting;

public enum PlantModifier {
    GIANT(ChatFormatting.DARK_AQUA),
    SPEEDY(ChatFormatting.GREEN),
    POWERFUL(ChatFormatting.RED),
    LUCKY(ChatFormatting.GOLD);

    private final ChatFormatting color;

    PlantModifier(ChatFormatting color) { this.color = color; }
    public ChatFormatting getColor() { return color; }
    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
}
