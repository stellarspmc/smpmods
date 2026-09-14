package spmc.smpmod.utils

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.item.Item
import net.minecraft.world.level.levelgen.Heightmap
import spmc.smpmod.SMPMod.Companion.minecraftServer
import java.util.regex.Pattern
import kotlin.math.roundToInt

@Suppress("UnstableApiUsage")
fun isAdmin(player: ServerPlayer) = player.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)
fun streamToSuggestion(set: Set<Item>): SuggestionProvider<CommandSourceStack> = { _, builder -> SharedSuggestionProvider.suggestResource(set.distinct().map { BuiltInRegistries.ITEM.getKey(it) }, builder)}
fun rnd2DP(toBeRounded: Double) = (toBeRounded * 100.0).roundToInt() / 100.0
fun getY(x: Int, z: Int, level: ServerLevel) = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)
fun checkNotCreative(players: Collection<ServerPlayer>) = players.all { it.level().dimension().identifier().namespace == "minecraft" }
fun checkNotCreative(vararg players: ServerPlayer) = checkNotCreative(players.asList())
fun id(id: String) = Identifier.fromNamespaceAndPath("smpmod", id)

@Suppress("UNCHECKED_CAST") fun <T> sendError(players: Collection<ServerPlayer>, message: String, returnValue: T = -1 as T): T { players.forEach { it.sendSystemMessage(Component.literal("✖: $message").withStyle(ChatFormatting.RED)) }; return returnValue }
@Suppress("UNCHECKED_CAST") fun <T> sendSuccess(players: Collection<ServerPlayer>, message: String, returnValue: T = 1 as T): T { players.forEach { it.sendSystemMessage(Component.literal("✔: $message").withStyle(ChatFormatting.GREEN)) }; return returnValue }
@Suppress("UNCHECKED_CAST") fun <T> sendError(vararg player: ServerPlayer, message: String, returnValue: T = -1 as T): T { return sendError(player.asList(), message, returnValue) }
@Suppress("UNCHECKED_CAST") fun <T> sendSuccess(vararg player: ServerPlayer, message: String, returnValue: T = 1 as T): T { return sendSuccess(player.asList(), message, returnValue) }

fun parseMarkdown(message: String): String {
	var message = message
	message = replaceWith(message, "(?<!\\\\)\\*\\*", ChatFormatting.BOLD.toString(), ChatFormatting.RESET.toString())
	message = replaceWith(message, "(?<!\\\\)\\*", ChatFormatting.ITALIC.toString(), ChatFormatting.RESET.toString())
	message = replaceWith(message, "(?<!\\\\)__", ChatFormatting.UNDERLINE.toString(), ChatFormatting.RESET.toString())
	message = replaceWith(message, "(?<!\\\\)_", ChatFormatting.ITALIC.toString(), ChatFormatting.RESET.toString())
	message = replaceWith(message, "(?<!\\\\)~~", ChatFormatting.STRIKETHROUGH.toString(), ChatFormatting.RESET.toString())

	return message.replace("\\\\\\*".toRegex(), "*").replace("\\\\_".toRegex(), "_").replace("\\\\~".toRegex(), "~").replace("\"".toRegex(), "\\\\\"")
}

fun formatName(id: String): String {
	val words = id.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
	val sb = StringBuilder()
	for (word in words) if (word.isNotEmpty()) sb.append(word[0].uppercaseChar()).append(word.substring(1)).append(" ")
	return sb.toString().trim { it <= ' ' }
}

private fun replaceWith(message: String, quot: String, pre: String, suf: String): String {
	var part = message
	for (str in getMatches(message, "$quot(.+?)$quot")) part = part.replaceFirst((quot + Pattern.quote(str) + quot).toRegex(), pre + str + suf)
	return part
}

private fun getMatches(string: String, regex: String): MutableList<String> {
	val matcher = Pattern.compile(regex).matcher(string)
	val matches: MutableList<String> = ArrayList()

	while (matcher.find()) matches.add(matcher.group(1))
	return matches
}

fun grant(player: ServerPlayer, advancementPath: String) {
	val id = Identifier.fromNamespaceAndPath("smpmod", advancementPath)
	val advancementHolder = minecraftServer?.advancements?.get(id) ?: return
	val progress = player.advancements.getOrStartProgress(advancementHolder)

	if (!progress.isDone) for (criterion in progress.remainingCriteria) player.advancements.award(advancementHolder, criterion)
}