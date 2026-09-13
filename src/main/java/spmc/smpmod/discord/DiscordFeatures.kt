package spmc.smpmod.discord

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.fabricmc.loader.api.FabricLoader
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import spmc.smpmod.SMPMod
import java.io.IOException
import java.nio.file.Files

object ConfigLoader {
	private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
	private val CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("smpmod_discord.json")

	var CONFIG: DiscordConfig? = DiscordConfig()

	fun saveConfig() { try { Files.newBufferedWriter(CONFIG_FILE).use { GSON.toJson(CONFIG, it) }} catch (e: Exception) { SMPMod.modLogger.error("Failed to save Discord config!", e) }}
	fun checkConfigs() {
		if (Files.exists(CONFIG_FILE)) loadConfig()
		else {
			saveConfig()
			SMPMod.modLogger.warn("Created default Discord config file. Please fill in your Bot Token and Channel IDs at: {}", CONFIG_FILE.fileName)
		}
	}

	private fun loadConfig() {
		try {
			Files.newBufferedReader(CONFIG_FILE).use { CONFIG = GSON.fromJson<DiscordConfig?>(it, DiscordConfig::class.java)?: DiscordConfig() }
		} catch (e: Exception) { SMPMod.modLogger.error("Failed to load Discord config! Reverting to defaults.", e) }
	}
}

class DiscordConfig @JvmOverloads constructor(webhook: String? = null, token: String? = null, messageChannelId: String? = null) {
	val webhook: String = webhook?: ""
	val token: String = token?: ""
	val messageChannelId = messageChannelId?: ""
}

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