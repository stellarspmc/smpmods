package spmc.smpmod.utils

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.Item
import spmc.smpmod.SMPMod
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Stream

object UtilityFunctions {
    fun getLevelOfEntity(uuid: UUID): ServerLevel {
        val level = AtomicReference<ServerLevel>()
        SMPMod.minecraftServer?.allLevels?.forEach { a -> if (a.getEntity(uuid) != null) level.set(a) }
	    return level.get()
    }

	// TODO: could be further optimized using kt functions
    fun streamToSuggestion(itemStream: Stream<Item>): SuggestionProvider<CommandSourceStack> = { _, builder -> SharedSuggestionProvider.suggestResource(itemStream.distinct().map { thing -> BuiltInRegistries.ITEM.getKey(thing) }, builder)}
}
