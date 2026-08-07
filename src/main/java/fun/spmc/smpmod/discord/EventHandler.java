package fun.spmc.smpmod.discord;

import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.economy.fluctuate.FluctuationData;
import fun.spmc.smpmod.economy.fluctuate.MarketState;
import fun.spmc.smpmod.utils.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
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
                .append(Component.literal(MessageUtils.parseMarkdown(message)))));
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
                    .map(player -> MarkdownSanitizer.escape(player.getGameProfile().name()))
                    .collect(Collectors.joining(", "));

            if (playerList.isEmpty()) playerList = "*No players online right now.*";

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Server Status")
                    .setColor(0x2F3136)
                    .setDescription(String.format("**%d** players currently in the SMP.", onlineCount))
                    .addField("Online List", playerList, false)
                    .build();

            event.replyEmbeds(embed).queue();

        } else if (event.getName().equals("top")) {
            OptionMapping pageOption = event.getOption("page");
            int page = pageOption != null ? pageOption.getAsInt() : 1;

            EconomyData eco = EconomyData.get();
            String leaderboardData = eco.top(page);

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Wealth Leaderboard")
                    .setColor(0xDFC66F)
                    .setDescription(leaderboardData)
                    .setFooter(String.format("Page %d", page), null)
                    .build();

            event.replyEmbeds(embed).queue();
        } else if (event.getName().equals("market")) {
            MarketState market = MarketState.getState();
            StringBuilder description = new StringBuilder();
            market.getAll().entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue().getDefaultPrice(), e1.getValue().getDefaultPrice()))
                    .forEach(entry -> {
                        Item item = entry.getKey();
                        FluctuationData data = entry.getValue();

                        double buyUnit = data.getBulkBuyCost(1);
                        double sellUnit = data.getBulkSellPayout(1);
                        double ratio = (data.getCurrentPrice() / data.getDefaultPrice() - 1) * 100.0;

                        description.append(String.format("• **%s** | Buy: **$%.2f** | Sell: **$%.2f** `%s`\n",
                                Component.translatable(item.getDescriptionId()).getString(), buyUnit, sellUnit, ratio >= 0 ? String.format("(+%.1f%%)", ratio) : String.format(" (%.1f%%)", ratio)));
                    });
            String resultText = !description.isEmpty() ? description.toString() : "*No items listed on the market.*";
            if (resultText.length() > 4096) resultText = resultText.substring(0, 4090) + "...";
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Market Prices")
                    .setColor(0xDFC66F)
                    .setDescription(resultText)
                    .build();

            event.replyEmbeds(embed).queue();
        }
    }
}