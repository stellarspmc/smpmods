package fun.spmc.smpmod.discord;

import fun.spmc.smpmod.discord.utils.MarkdownParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static fun.spmc.smpmod.SMPMod.*;

public class EventHandler extends ListenerAdapter {
    private static void broadcastMessage(String discordTags, String message) {
        modLogger.info(String.format("[Discord] %s: %s", discordTags, message));
        minecraftServer.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(Text.literal("[")
                .append(Text.literal("Discord").withColor(88 * 65536 + 101 * 256 + 242))
                .append(Text.literal("] "))
                .append(Text.literal(discordTags).withColor(88 * 65536 + 101 * 256 + 242))
                .append(Text.literal(": "))
                .append(Text.literal(MarkdownParser.parseMarkdown(message)))));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getChannel() != messageChannel || e.getAuthor().isBot() || minecraftServer == null) return;
        if (e.getMessage().getAttachments().isEmpty() && e.getMessage().getStickers().isEmpty()) broadcastMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
        else if (!e.getMessage().getStickers().isEmpty() && e.getMessage().getAttachments().isEmpty()) broadcastMessage(e.getAuthor().getName(), "<sticker>");
        else if (!e.getMessage().getAttachments().isEmpty()) broadcastMessage(e.getAuthor().getName(), "<attachment>");
        else broadcastMessage(e.getAuthor().getName(), "<other>");
    }
}