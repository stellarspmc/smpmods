package spmc.smpmod.discord.config

class DiscordConfig @JvmOverloads constructor(webhook: String? = null, token: String? = null, messageChannelId: String? = null) {
	val webhook: String = webhook?: ""
	val token: String = token?: ""
	val messageChannelId = messageChannelId?: ""
}