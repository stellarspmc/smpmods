package `fun`.spmc.smpmod.core

import net.minecraft.network.chat.TextColor
import java.util.*

enum class ItemRarity(@JvmField val color: TextColor, private val consideredRare: Boolean) {
    COMMON(TextColor.WHITE, false),  // T1
    UNCOMMON(TextColor.GREEN, false),  // T2
    RARE(TextColor.BLUE, false),  // T3
    EPIC(TextColor.DARK_PURPLE, false),  // T4
    LEGENDARY(TextColor.GOLD, true),  // T5
    MYTHIC(TextColor.LIGHT_PURPLE, true),  // T6
    CHROMATIC(TextColor.RED, true),  // T7
    ASTRAL(TextColor.AQUA, true); // T8

    fun shouldAnnounce(): Boolean { return consideredRare }
    override fun toString(): String { return name[0].toString() + name.substring(1).lowercase(Locale.getDefault()) }
}
