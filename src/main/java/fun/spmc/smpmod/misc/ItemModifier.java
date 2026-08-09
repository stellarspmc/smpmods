package fun.spmc.smpmod.misc;

import net.minecraft.network.chat.TextColor;
import java.util.Optional;

public enum ItemModifier {
    GIANT(TextColor.fromRgb(0x2B7A78), 1.2),
    SPEEDY(TextColor.fromRgb(0x38B000), 2.1),
    POWERFUL(TextColor.fromRgb(0xE63946), 1.6),
    STUPID(TextColor.fromRgb(0x8D5B4C), .7),
    GOLDEN(TextColor.fromRgb(0xFFC300), 2.0),
    CRYSTALLIZED(TextColor.fromRgb(0xE056FD), 2.3),
    BLESSED(TextColor.fromRgb(0xF7D674), 1.5),
    GODLY(TextColor.fromRgb(0xFFD700), 2.4),
    EATEN(TextColor.fromRgb(0x3A3A3A), .3),
    ULTIMATE(TextColor.fromRgb(0x4361EE), 4),
    EVIL(TextColor.fromRgb(0x800020), .7),
    SHELDONED(TextColor.fromRgb(0x2EC4B6), .8),
    PUFFERED(TextColor.fromRgb(0xFFB703), 1.1),
    ECHO(TextColor.fromRgb(0x00A896), 1.45),
    PILLAGED(TextColor.fromRgb(0x9E2A2B), .5),
    BRUCED(TextColor.fromRgb(0x10B981), 2.5),
    BRUISED(TextColor.fromRgb(0x581845), .6),
    VIVID(TextColor.fromRgb(0xF72585), 1.2),
    COLORFUL(TextColor.fromRgb(0xFF70A6), 3),
    NUCLEAR(TextColor.fromRgb(0x39FF14), 3.25),
    LUCKY(TextColor.fromRgb(0x00C853), 1.4);
    private final TextColor color;
    private final double priceMultiplier;
    ItemModifier(TextColor color, double priceMultiplier) { this.color = color; this.priceMultiplier = priceMultiplier; }
    public TextColor getColor() { return color; }
    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    public double getPriceMultiplier() { return priceMultiplier; }
    public boolean isNotLocked() { return !(getPriceMultiplier() >= 2); }

    public static Optional<ItemModifier> fromId(String id) {
        for (ItemModifier mod : values()) if (mod.name().equalsIgnoreCase(id)) return Optional.of(mod);
        return Optional.empty();
    }
}
