package fun.spmc.smpmod.discord;

import fun.spmc.smpmod.discord.utils.MarkdownParser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static fun.spmc.smpmod.SMPMod.*;

public class EventHandler extends ListenerAdapter {
    private static void broadcastMessage(String discordTags, String message) {
        modLogger.info("[Discord] {}: {}", discordTags, message);

        Style finalStyle = Text.empty().getStyle().withColor(TextColor.fromRgb(88 * 65536 + 101 * 256 + 242));
        minecraftServer.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(Text.literal("[")
                .append(Text.literal("Discord").setStyle(finalStyle))
                .append(Text.literal("] "))
                .append(Text.literal(discordTags).setStyle(finalStyle))
                .append(Text.literal(": "))
                .append(Text.literal(MarkdownParser.parseMarkdown(message)))));
    }

    private static boolean moderateMessage(Message message) {
        if (message.getAuthor().isBot() || message.isWebhookMessage()) return true;
        Member member = Objects.requireNonNull(message.getGuild().getMember(message.getAuthor()));

        if (member.getPermissions().contains(Permission.ADMINISTRATOR)) return true;

        if (!message.getEmbeds().isEmpty()) {
            message.delete().queue();
            member.ban(3, TimeUnit.DAYS).queue();
            return false;
        }

        if (message.getMentions().getMembers().size() > 5) {
            message.delete().queue();
            member.timeoutFor(Duration.ofSeconds(30)).queue();
            return false;
        }

        if (!message.getInvites().isEmpty()) {
            message.delete().queue();
            member.timeoutFor(Duration.ofSeconds(60)).queue();
            return false;
        }

        return true;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getChannel() != messageChannel || e.getAuthor().isBot() || minecraftServer == null) return;

        if (moderateMessage(e.getMessage())) {
            if (e.getMessage().getAttachments().isEmpty() && e.getMessage().getStickers().isEmpty()) broadcastMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
            else if (!e.getMessage().getStickers().isEmpty() && e.getMessage().getAttachments().isEmpty()) broadcastMessage(e.getAuthor().getName(), "<sticker>");
            else if (!e.getMessage().getAttachments().isEmpty()) broadcastMessage(e.getAuthor().getName(), "<attachment>");
            else broadcastMessage(e.getAuthor().getName(), "<other>");
        }
    }
}