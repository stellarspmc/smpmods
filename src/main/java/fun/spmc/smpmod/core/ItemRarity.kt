package `fun`.spmc.smpmod.core

import net.minecraft.network.chat.TextColor
import java.util.*

enum class ItemRarity(@JvmField val color: TextColor, private val consideredRare: Boolean) {
    COMMON(TextColor.fromRgb(10529458), false),
    UNCOMMON(TextColor.fromRgb(3066993), false),
    RARE(TextColor.fromRgb(3377407), false),
    EPIC(TextColor.fromRgb(10837738), false),
    LEGENDARY(TextColor.fromRgb(16757058), true),
    MYTHIC(TextColor.fromRgb(16740518), true),
    CHROMATIC(TextColor.fromRgb(16726072), true),
    ASTRAL(TextColor.fromRgb(4581119), true);

    fun shouldAnnounce(): Boolean { return consideredRare }
    override fun toString(): String { return name[0].toString() + name.substring(1).lowercase(Locale.getDefault()) }
}
