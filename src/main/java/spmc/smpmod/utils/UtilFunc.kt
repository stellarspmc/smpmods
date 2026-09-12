package spmc.smpmod.utils

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.Heightmap
import kotlin.math.roundToInt

object UtilFunc {
	@Suppress("UnstableApiUsage")
	fun isAdmin(player: ServerPlayer) = player.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)
	fun streamToSuggestion(set: Set<Item>): SuggestionProvider<CommandSourceStack> = { _, builder -> SharedSuggestionProvider.suggestResource(set.distinct().map { BuiltInRegistries.ITEM.getKey(it) }, builder)}
	fun createBiomeTag(tag: String): TagKey<Biome> = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", tag)) // TODO: remove
	fun rnd2DP(toBeRounded: Double) = (toBeRounded * 100.0).roundToInt() / 100.0
	fun getY(x: Int, z: Int, level: ServerLevel) = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)
}