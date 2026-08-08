package fun.spmc.smpmod.fishing;

import net.minecraft.ChatFormatting;

import java.util.Optional;

public enum FishModifier {
    GIANT(ChatFormatting.DARK_AQUA,1),
    SPEEDY(ChatFormatting.GREEN, .9),
    POWERFUL(ChatFormatting.RED, 1.4),
    STUPID(ChatFormatting.DARK_PURPLE, .7),
    LUCKY(ChatFormatting.GOLD, 1.1);

    private final ChatFormatting color;
    private final double priceMultiplier;
    FishModifier(ChatFormatting color, double priceMultiplier) { this.color = color; this.priceMultiplier = priceMultiplier; }
    public ChatFormatting getColor() { return color; }
    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    public double getPriceMultiplier() { return priceMultiplier; }

    public static Optional<FishModifier> fromId(String id) {
        for (FishModifier mod : values()) if (mod.name().equalsIgnoreCase(id)) return Optional.of(mod);
        return Optional.empty();
    }
}
