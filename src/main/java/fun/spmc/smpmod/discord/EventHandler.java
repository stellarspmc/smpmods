package fun.spmc.smpmod.discord;

import fun.spmc.smpmod.discord.utils.MarkdownParser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static fun.spmc.smpmod.SMPMod.*;

public class EventHandler extends ListenerAdapter {
    private static void broadcastMessage(String discordTags, String message) {
        modLogger.info("[Discord] {}: {}", discordTags, message);

        Style finalStyle = Component.empty().getStyle().withColor(TextColor.fromRgb(88 * 65536 + 101 * 256 + 242));
        minecraftServer.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.literal("[")
                .append(Component.literal("Discord").setStyle(finalStyle))
                .append(Component.literal("] "))
                .append(Component.literal(discordTags).setStyle(finalStyle))
                .append(Component.literal(": "))
                .append(Component.literal(MarkdownParser.parseMarkdown(message)))));
    }
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getChannel() != messageChannel || e.getAuthor().isBot() || minecraftServer == null) return;

        if (e.getMessage().getAttachments().isEmpty() && e.getMessage().getStickers().isEmpty()) broadcastMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
        else if (!e.getMessage().getStickers().isEmpty() && e.getMessage().getAttachments().isEmpty()) broadcastMessage(e.getAuthor().getName(), "<sticker>");
        else if (!e.getMessage().getAttachments().isEmpty()) broadcastMessage(e.getAuthor().getName(), "<attachment>");
        else broadcastMessage(e.getAuthor().getName(), "<other>");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("players"))
            event.reply(minecraftServer.getPlayerCount() + " Players, Players inside: `" + minecraftServer.getPlayerList().getPlayers().stream().map(player -> player.getGameProfile().name()).collect(Collectors.joining(", ")) + "`").queue();
    }
}