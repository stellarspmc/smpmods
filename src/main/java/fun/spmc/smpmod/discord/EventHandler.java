package fun.spmc.smpmod.discord;

import fun.spmc.smpmod.discord.utils.MarkdownParser;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
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
        if (event.getName().equals("players")) {
            int onlineCount = minecraftServer.getPlayerCount();
            String playerList = minecraftServer.getPlayerList().getPlayers().stream()
                    .map(player -> player.getGameProfile().name())
                    .collect(Collectors.joining(", "));

            if (playerList.isEmpty()) {
                playerList = "*No players online right now.*";
            }

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("🟢 Server Status")
                    .setColor(0x2F3136)
                    .setDescription(String.format("**%d** players currently exploring.", onlineCount))
                    .addField("Online List", playerList, false)
                    .build();

            event.replyEmbeds(embed).queue();

        } else if (event.getName().equals("top")) {
            OptionMapping pageOption = event.getOption("page");
            int page = pageOption != null ? pageOption.getAsInt() : 1;

            ServerLevel overworld = minecraftServer.overworld();
            EconomySavedData eco = EconomySavedData.get(overworld);
            String leaderboardData = eco.top(page);

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("🏆 Wealth Leaderboard")
                    .setColor(0xDFC66F)
                    .setDescription(leaderboardData)
                    .setFooter(String.format("Page %d", page), null)
                    .build();

            event.replyEmbeds(embed).queue();
        }
    }
}