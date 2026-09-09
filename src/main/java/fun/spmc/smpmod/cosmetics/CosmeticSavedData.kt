package `fun`.spmc.smpmod.cosmetics

import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.ServerPlaceholderContext
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.saveddata.SavedData

object CosmeticSavedData : SavedData() {
    fun register() {
        Placeholders.registerServer<Any?>(
            Identifier.fromNamespaceAndPath("smpmod", "prefix")
        ) { context: ServerPlaceholderContext?, _: String? ->
            if (context!!.hasPlayer()) {
                val prefix = getEquippedPrefix(context.player() as ServerPlayer?)
                if (prefix.string.isNotBlank()) return@registerServer PlaceholderResult.value(prefix)
            }
            PlaceholderResult.value(Component.empty())
        }
    }

    private fun getEquippedPrefix(player: ServerPlayer?): Component {
        return Component.empty().append("")
    }
}
