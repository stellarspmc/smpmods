package spmc.smpmod.discord

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import spmc.smpmod.SMPMod
import spmc.smpmod.discord.config.ConfigLoader
import java.io.IOException

object DiscordWebhook {
	private val HTTP_CLIENT = OkHttpClient()

	fun sendChatMessage(message: String, playerName: String, playerUUID: String) {
		val webhookUrl = ConfigLoader.CONFIG?.webhook?.ifEmpty{ return } ?: return

		val allowedMentions = JsonObject()
		allowedMentions.add("parse", JsonArray())

		val body = JsonObject()
		body.addProperty("content", message)
		body.addProperty("username", playerName)
		body.addProperty("avatar_url", "https://mc-heads.net/head/$playerUUID/512.png")
		body.add("allowed_mentions", allowedMentions)

		HTTP_CLIENT.newCall(Request.Builder().url(webhookUrl).post(body.toString().toRequestBody("application/json".toMediaType())).build()).enqueue(object: Callback {
			override fun onFailure(call: Call, e: IOException) { SMPMod.modLogger.error("Failed to send Discord webhook message", e) }
			override fun onResponse(call: Call, response: Response) { response.close() }
		})
	}
}
