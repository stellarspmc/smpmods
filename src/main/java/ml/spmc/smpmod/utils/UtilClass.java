package ml.spmc.smpmod.utils;

import eu.pb4.placeholders.api.TextParserUtils;

import java.util.Random;

import static ml.spmc.smpmod.SMPMod.modLogger;
import static ml.spmc.smpmod.SMPMod.minecraftServer;

public class UtilClass {
    public static boolean lockdown = false;

    public static void broadcastMessage(String discordTags, String message) {
        modLogger.info(("[Discord] " + discordTags + ": " + message));
        minecraftServer.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(TextParserUtils.formatText("[<dark_purple>Discord</dark_purple>] <dark_purple>" + MarkdownParser.parseMarkdown(discordTags + "</dark_purple>: " + MarkdownParser.parseMarkdown(message)))));
    }

    public static boolean probabilityCalc(double percentage) {
        Random random = new Random();
        double prob = random.nextDouble();
        return prob <= (percentage * 0.01);
    }

    public static void errorLog(String message) {
        modLogger.error("==============================================");
        modLogger.error("");
        modLogger.error(message);
        modLogger.error("");
        modLogger.error("==============================================");

    }
}
