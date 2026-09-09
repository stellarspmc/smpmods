package `fun`.spmc.smpmod.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import `fun`.spmc.smpmod.SMPMod
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.Item
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Stream

object UtilityFunctions {
    fun getLevelOfEntity(uuid: UUID): ServerLevel {
        val level = AtomicReference<ServerLevel>()
        SMPMod.minecraftServer?.allLevels?.forEach(Consumer { a: ServerLevel -> if (a.getEntity(uuid) != null) level.set(a) })
        return level.get()
    }

    fun streamToSuggestion(itemStream: Stream<Item>): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { _: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder ->
            SharedSuggestionProvider.suggestResource(itemStream.distinct().map { thing: Item? -> BuiltInRegistries.ITEM.getKey(thing!!) }, builder)
        }
    }
}
