package spmc.smpmod.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.EconomyData.Companion.get
import spmc.smpmod.economy.fluctuate.MarketState.Companion.state
import spmc.smpmod.utils.MessageUtils.parseMarkdown
import kotlin.String

class EventHandler: ListenerAdapter() {
	override fun onMessageReceived(e: MessageReceivedEvent) {
		if (e.getChannel() !== SMPMod.messageChannel || e.author.isBot || SMPMod.minecraftServer == null) return

		if (e.message.attachments.isEmpty() && e.message.stickers.isEmpty()) broadcastMessage(e.author.name, e.message.contentStripped)
		else if (e.message.stickers.isNotEmpty() && e.message.attachments.isEmpty()) broadcastMessage(e.author.name, "<sticker>")
		else if (e.message.attachments.isNotEmpty()) broadcastMessage(e.author.name, "<attachment>")
		else broadcastMessage(e.author.name, "<other>")
	}

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		when (event.name) {
			"players" -> event.replyEmbeds(EmbedBuilder().setTitle("Server Status").setColor(0x2F3136).setDescription(String.format("**%d** players currently in the SMP.", SMPMod.minecraftServer?.playerCount ?: return)).addField("Online List", SMPMod.minecraftServer?.playerList?.players?.joinToString(", ") { player -> MarkdownSanitizer.escape(player.gameProfile.name()) }?.ifEmpty { "*No players online right now.*" } ?: return, false).build()).queue()
			"top" -> {
				val page = event.getOption("page")?.asInt ?: 1
				val eco = get()?: return

				event.replyEmbeds(EmbedBuilder().setTitle("Wealth Leaderboard").setColor(0xDFC66F).setDescription(eco.top(page)).setFooter(String.format("Page %d", page), null).build()).queue()
			}
			"market" -> {
				val market = state?: return
				val description = StringBuilder()
				market.all.entries.sortedBy { it.value.currentPrice }.asReversed().forEach {
					val data = it.value
					val buyUnit = data.getBulkBuyCost(1)
					val sellUnit = data.getBulkSellPayout(1)
					val ratio = (data.currentPrice / data.defaultPrice - 1) * 100.0
					description.append(String.format("• **%s** | Buy: **$%.2f** | Sell: **$%.2f** `%s`\n", Component.translatable(it.key.getDescriptionId()).string, buyUnit, sellUnit, if (ratio >= 0) String.format("(+%.1f%%)", ratio) else String.format(" (%.1f%%)", ratio)))
				}
				var resultText = if (description.isNotEmpty()) description.toString() else "*No items listed on the market.*"
				if (resultText.length > 4096) resultText = resultText.substring(0, 4090) + "..."
				event.replyEmbeds(EmbedBuilder().setTitle("Market Prices").setColor(0xDFC66F).setDescription(resultText).build()).queue()
			}
		}
	}

	companion object {
		private fun broadcastMessage(discordTags: String, message: String) {
			SMPMod.modLogger.info("[Discord] {}: {}", discordTags, message)
			val finalStyle = Component.empty().style.withColor(TextColor.fromRgb(88 * 65536 + 101 * 256 + 242))
			SMPMod.minecraftServer?.playerList?.players?.forEach { player -> player.sendSystemMessage(Component.literal("[").append(Component.literal("Discord").setStyle(finalStyle)).append(Component.literal("] ")).append(Component.literal(discordTags).setStyle(finalStyle)).append(Component.literal(": ")).append(Component.literal(parseMarkdown(message)))) }
		}
	}
}