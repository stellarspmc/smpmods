package ml.spmc.smpmod.utils;

import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Xujiayao
 */
public class MarkdownParser {

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

		for (String str : getMatches(message, quot + "(.+?)" + quot)) {
			part = part.replaceFirst(quot + Pattern.quote(str) + quot, pre + str + suf);
		}

		return part;
	}

	private static List<String> getMatches(String string, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		List<String> matches = new ArrayList<>();

		while (matcher.find()) {
			matches.add(matcher.group(1));
		}

		return matches;
	}
}
