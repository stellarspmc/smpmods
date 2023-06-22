package ml.spmc.smpmod.utils;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static ml.spmc.smpmod.SMPMod.LOGGER;

public class ConfigLoader {

    public static String WEBHOOK_URL;
    public static String AVATAR_URL;
    public static String BOT_TOKEN;
    public static String MESSAGE_CHANNEL_ID;
    public static String MUSIC_CHANNEL_ID;
    public static String GUILD_ID;
    public static String APPEAL_CHANNEL_ID;


    // ran when start
    public static void checkConfigs() {
        try {
            Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve("smpmods.properties");
            if (!Files.exists(configFilePath)) {
                LOGGER.info("Creating Config File...");
                Files.createFile(configFilePath);

                FileWriter myWriter = new FileWriter(configFilePath.toFile());
               myWriter.write(
                       "// URLs\n" +
                          "webhook_url=null\n" +
                          "avatar_url=null\n" +
                          "// Discord Stuff\n" +
                          "bot_token=null\n" +
                          "message_channel_id=null\n" +
                          "music_channel_id=null\n" +
                          "guild_id=null\n" +
                          "appeal_channel_id=null");
               myWriter.close();
            }

            FileInputStream propsInput = new FileInputStream(configFilePath.toFile());
            Properties prop = new Properties();
            prop.load(propsInput);

            WEBHOOK_URL = prop.getProperty("webhook_url");
            AVATAR_URL = prop.getProperty("avatar_url");
            BOT_TOKEN = prop.getProperty("bot_token");
            MESSAGE_CHANNEL_ID = prop.getProperty("message_channel_id");
            MUSIC_CHANNEL_ID = prop.getProperty("music_channel_id");
            GUILD_ID = prop.getProperty("guild_id");
            APPEAL_CHANNEL_ID = prop.getProperty("appeal_channel_id");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
