package fun.spmc.smpmod.discord.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static fun.spmc.smpmod.SMPMod.modLogger;

public class ConfigLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("smpmod_discord.json");

    public static DiscordConfig CONFIG = new DiscordConfig();

    public static void checkConfigs() {
        if (Files.exists(CONFIG_FILE)) loadConfig();
        else {
            saveConfig();
            modLogger.warn("Created default Discord config file. Please fill in your Bot Token and Channel IDs at: {}", CONFIG_FILE.getFileName());
        }
    }

    private static void loadConfig() {
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            CONFIG = GSON.fromJson(reader, DiscordConfig.class);
            if (CONFIG == null) CONFIG = new DiscordConfig();
        } catch (Exception e) {
            modLogger.error("Failed to load Discord config! Reverting to defaults.", e);
        }
    }

    public static void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(CONFIG, writer);
        } catch (Exception e) {
            modLogger.error("Failed to save Discord config!", e);
        }
    }
}