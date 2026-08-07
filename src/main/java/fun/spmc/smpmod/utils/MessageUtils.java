package fun.spmc.smpmod.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    public static void sendErrorMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("✖: " + message).withStyle(ChatFormatting.RED));
    }

    public static void sendSuccessMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("✔: " + message).withStyle(ChatFormatting.GREEN));
    }

    public static String parseMarkdown(String message) {
        message = replaceWith(message, "(?<!\\\\)\\*\\*", ChatFormatting.BOLD.toString(), ChatFormatting.RESET.toString());
        message = replaceWith(message, "(?<!\\\\)\\*", ChatFormatting.ITALIC.toString(), ChatFormatting.RESET.toString());
        message = replaceWith(message, "(?<!\\\\)__", ChatFormatting.UNDERLINE.toString(), ChatFormatting.RESET.toString());
        message = replaceWith(message, "(?<!\\\\)_", ChatFormatting.ITALIC.toString(), ChatFormatting.RESET.toString());
        message = replaceWith(message, "(?<!\\\\)~~", ChatFormatting.STRIKETHROUGH.toString(), ChatFormatting.RESET.toString());

        message = message.replaceAll("\\\\\\*", "*").replaceAll("\\\\_", "_").replaceAll("\\\\~", "~");
        message = message.replaceAll("\"", "\\\\\"");
        return message;
    }

    private static String replaceWith(String message, String quot, String pre, String suf) {
        String part = message;
        for (String str : getMatches(message, quot + "(.+?)" + quot))
            part = part.replaceFirst(quot + Pattern.quote(str) + quot, pre + str + suf);

        return part;
    }

    private static List<String> getMatches(String string, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        List<String> matches = new ArrayList<>();

        while (matcher.find()) matches.add(matcher.group(1));
        return matches;
    }
}
