package fun.spmc.smpmod.misc;

import net.minecraft.ChatFormatting;

import java.util.Optional;

public enum ItemModifier {
    GIANT(ChatFormatting.DARK_AQUA, 1.2),
    SPEEDY(ChatFormatting.GREEN, .9),
    POWERFUL(ChatFormatting.RED, 1.6),
    STUPID(ChatFormatting.DARK_PURPLE, .7),
    GOLDEN(ChatFormatting.GOLD, 2),
    CRYSTALLIZED(ChatFormatting.LIGHT_PURPLE, 2.3),
    BLESSED(ChatFormatting.GOLD, 1.5),
    GODLY(ChatFormatting.YELLOW, 2.4),
    EATEN(ChatFormatting.BLACK, .3),
    ULTIMATE(ChatFormatting.BOLD, 3),
    EVIL(ChatFormatting.DARK_RED, .7),
    SHELDONED(ChatFormatting.AQUA, .8),
    PUFFERED(ChatFormatting.YELLOW, 1.1),
    ECHO(ChatFormatting.BLUE, 1.45),
    PILLAGED(ChatFormatting.DARK_RED, .5),
    BRUCED(ChatFormatting.GREEN, 2.5),
    BRUISED(ChatFormatting.DARK_RED, .6),
    VIVID(ChatFormatting.LIGHT_PURPLE, 1.2),
    LUCKY(ChatFormatting.GOLD, 1.4);

    private final ChatFormatting color;
    private final double priceMultiplier;
    ItemModifier(ChatFormatting color, double priceMultiplier) { this.color = color; this.priceMultiplier = priceMultiplier; }
    public ChatFormatting getColor() { return color; }
    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    public double getPriceMultiplier() { return priceMultiplier; }

    public static Optional<ItemModifier> fromId(String id) {
        for (ItemModifier mod : values()) if (mod.name().equalsIgnoreCase(id)) return Optional.of(mod);
        return Optional.empty();
    }
}
