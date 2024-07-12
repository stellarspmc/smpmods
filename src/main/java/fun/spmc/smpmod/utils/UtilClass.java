package fun.spmc.smpmod.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static fun.spmc.smpmod.SMPMod.modLogger;
import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class UtilClass {

    public static void broadcastMessage(String discordTags, String message) {
        modLogger.info(String.format("[Discord] %s: %s", discordTags, message));
        minecraftServer.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(Text.literal("[")
                .append(Text.literal("Discord").withColor(118 * 65536 + 15 * 256 + 191))
                .append(Text.literal("]"))
                .append(Text.literal(discordTags).withColor(118 * 65536 + 15 * 256 + 191))
                .append(Text.literal(": "))
                .append(Text.literal(MarkdownParser.parseMarkdown(message)))));
    }

    public static boolean probabilityCalc(double percentage, PlayerEntity player) {
        return player.getRandom().nextDouble() <= (percentage * 0.01);
    }

    public static ItemStack getAndSetCount(ItemStack item, int count) {
        item.setCount(count);
        return item;
    }
}
