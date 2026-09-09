package `fun`.spmc.smpmod.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.regex.Pattern

object MessageUtils {
    fun <T> sendError(player: ServerPlayer, message: String, returnValue: T): T {
        player.sendSystemMessage(Component.literal("✖: $message").withStyle(ChatFormatting.RED))
        return returnValue
    }

    fun <T> sendSuccess(player: ServerPlayer, message: String, returnValue: T): T {
        player.sendSystemMessage(Component.literal("✔: $message").withStyle(ChatFormatting.GREEN))
        return returnValue
    }

    @JvmStatic
    fun parseMarkdown(message: String): String {
        var message = message
        message = replaceWith(message, "(?<!\\\\)\\*\\*", ChatFormatting.BOLD.toString(), ChatFormatting.RESET.toString())
        message = replaceWith(message, "(?<!\\\\)\\*", ChatFormatting.ITALIC.toString(), ChatFormatting.RESET.toString())
        message = replaceWith(message, "(?<!\\\\)__", ChatFormatting.UNDERLINE.toString(), ChatFormatting.RESET.toString())
        message = replaceWith(message, "(?<!\\\\)_", ChatFormatting.ITALIC.toString(), ChatFormatting.RESET.toString())
        message = replaceWith(message, "(?<!\\\\)~~", ChatFormatting.STRIKETHROUGH.toString(), ChatFormatting.RESET.toString())

        message = message.replace("\\\\\\*".toRegex(), "*").replace("\\\\_".toRegex(), "_").replace("\\\\~".toRegex(), "~")
        return message.replace("\"".toRegex(), "\\\\\"")
    }

    private fun replaceWith(message: String, quot: String, pre: String, suf: String): String {
        var part = message
        for (str in getMatches(message, "$quot(.+?)$quot")) part = part.replaceFirst((quot + Pattern.quote(str) + quot).toRegex(), pre + str + suf)
        return part
    }

    private fun getMatches(string: String, regex: String): MutableList<String> {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(string)
        val matches: MutableList<String> = ArrayList()

        while (matcher.find()) matches.add(matcher.group(1))
        return matches
    }

    @JvmStatic
    fun formatName(id: String): String {
        val words = id.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sb = StringBuilder()
        for (word in words) if (word.isNotEmpty()) sb.append(word[0].uppercaseChar()).append(word.substring(1)).append(" ")
        return sb.toString().trim { it <= ' ' }
    }
}
