package spmc.smpmod.core

import net.minecraft.network.chat.TextColor
import java.util.*

enum class ItemModifier(@JvmField val color: TextColor, @JvmField val priceMultiplier: Double) {
    GIANT(TextColor.fromRgb(0x2B7A78), 1.2),
    SPEEDY(TextColor.fromRgb(0x38B000), 2.1),
    POWERFUL(TextColor.fromRgb(0xE63946), 1.6),
    STUPID(TextColor.fromRgb(0x8D5B4C), .7),
    GOLDEN(TextColor.fromRgb(0xFFC300), 2.0),
    CRYSTALLIZED(TextColor.fromRgb(0xE056FD), 2.3),
    BLESSED(TextColor.fromRgb(0xF7D674), 1.5),
    GODLY(TextColor.fromRgb(0xFFD700), 2.4),
    EATEN(TextColor.fromRgb(0x3A3A3A), .3),
    ULTIMATE(TextColor.fromRgb(0x4361EE), 4.0),
    STARGAZED(TextColor.fromRgb(0xFEFEFE), 1.99),
    EVIL(TextColor.fromRgb(0x800020), .7),
    SHELDONED(TextColor.fromRgb(0x2EC4B6), .8),
    PUFFERED(TextColor.fromRgb(0xFFB703), 1.1),
    ECHO(TextColor.fromRgb(0x00A896), 1.45),
    PILLAGED(TextColor.fromRgb(0x9E2A2B), .5),
    BRUCED(TextColor.fromRgb(0x10B981), 2.5),
    BRUISED(TextColor.fromRgb(0x581845), .6),
    VIVID(TextColor.fromRgb(0xF72585), 1.2),
    COLORFUL(TextColor.fromRgb(0xFF70A6), 3.0),
    NUCLEAR(TextColor.fromRgb(0x39FF14), 3.25),
    LUCKY(TextColor.fromRgb(0x00C853), 1.4);

    val isNotLocked: Boolean get() = !(this.priceMultiplier >= 2)
    override fun toString(): String { return name[0].toString() + name.substring(1).lowercase(Locale.getDefault()) }

    companion object {
        @JvmStatic
        fun fromId(id: String): Optional<ItemModifier> {
            for (mod in entries) if (mod.name.equals(id, ignoreCase = true)) return Optional.of(mod)
            return Optional.empty()
        }
    }
}
