package ml.spmc.smpmod.utils;

import eu.pb4.placeholders.api.TextParserUtils;

import java.util.Random;

import static ml.spmc.smpmod.SMPMod.LOGGER;
import static ml.spmc.smpmod.SMPMod.SERVER;

public class UtilClass {
    public static boolean lockdown = false;

    public static void broadcastMessage(String discordTags, String message) {
        LOGGER.info(("[Discord] " + discordTags + ": " + message));
        SERVER.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(TextParserUtils.formatText("[<dark_purple>Discord</dark_purple>] <dark_purple>" + MarkdownParser.parseMarkdown(discordTags + "</dark_purple>: " + MarkdownParser.parseMarkdown(message)))));
    }

    public static boolean probabilityCalc(double percentage) {
        Random random = new Random();
        double prob = random.nextDouble();
        if (prob <= (percentage*0.01)) return true;
        return false;
    }
}
