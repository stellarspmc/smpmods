package fun.spmc.smpmod.utils;

import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.PlainTextContent;

import static fun.spmc.smpmod.SMPMod.modLogger;
import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class UtilClass {

    public static void broadcastMessage(String discordTags, String message) {
        modLogger.info(String.format("[Discord] %s: %s", discordTags, message));
        minecraftServer.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(TextParserUtils.formatText("[<dark_purple>Discord</dark_purple>] <dark_purple>" + discordTags + "</dark_purple>: " + MarkdownParser.parseMarkdown(message))));
    }

    public static boolean probabilityCalc(double percentage, PlayerEntity player) {
        return player.getRandom().nextDouble() <= (percentage * 0.01);
    }

    public static ItemStack getAndSetCount(ItemStack item, int count) {
        item.setCount(count);
        return item;
    }
}
