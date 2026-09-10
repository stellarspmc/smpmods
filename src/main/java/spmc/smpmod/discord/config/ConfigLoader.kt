package spmc.smpmod.discord.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import spmc.smpmod.SMPMod
import java.nio.file.Files

object ConfigLoader {
	private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
	private val CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("smpmod_discord.json")

	var CONFIG: DiscordConfig? = DiscordConfig()

	fun saveConfig() { try { Files.newBufferedWriter(CONFIG_FILE).use { writer -> GSON.toJson(CONFIG, writer) } } catch (e: Exception) { SMPMod.modLogger.error("Failed to save Discord config!", e) } }
	fun checkConfigs() {
		if (Files.exists(CONFIG_FILE)) loadConfig()
		else {
			saveConfig()
			SMPMod.modLogger.warn("Created default Discord config file. Please fill in your Bot Token and Channel IDs at: {}", CONFIG_FILE.fileName)
		}
	}

	private fun loadConfig() {
		try {
			Files.newBufferedReader(CONFIG_FILE).use { reader ->
				CONFIG = GSON.fromJson<DiscordConfig?>(reader, DiscordConfig::class.java)
				if (CONFIG == null) CONFIG = DiscordConfig()
			}
		} catch (e: Exception) { SMPMod.modLogger.error("Failed to load Discord config! Reverting to defaults.", e) }
	}
}