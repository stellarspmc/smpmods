package spmc.smpmod.utils

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.item.Item
import spmc.smpmod.SMPMod
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Stream

object UtilityFunctions {
    fun getLevelOfEntity(uuid: UUID): ServerLevel {
        val level = AtomicReference<ServerLevel>()
        SMPMod.minecraftServer?.allLevels?.forEach { if (it.getEntity(uuid) != null) level.set(it) }
	    return level.get()
    }

    fun streamToSuggestion(set: Set<Item>): SuggestionProvider<CommandSourceStack> = { _, builder -> SharedSuggestionProvider.suggestResource(set.distinct().map { BuiltInRegistries.ITEM.getKey(it) }, builder)}
	fun isAdmin(player: ServerPlayer) = player.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)
}
