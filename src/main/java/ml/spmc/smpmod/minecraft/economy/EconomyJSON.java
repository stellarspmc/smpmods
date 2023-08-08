package ml.spmc.smpmod.minecraft.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import static ml.spmc.smpmod.SMPMod.modLogger;

public class EconomyJSON {
    private static Path economyFilePath = FabricLoader.getInstance().getConfigDir().resolve("economy.json");

    public static void createFile() throws IOException {
        if (!Files.exists(economyFilePath)) {
            modLogger.info("Creating Economy File...");
            Files.createFile(economyFilePath);
        }
    }

    public static void addPlayer(ServerPlayerEntity player) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject object = gson.fromJson(Files.readString(economyFilePath), JsonObject.class);
        object.addProperty(player.getUuid().toString(), 0);
        gson.fromJson(object, (Type) new FileWriter(economyFilePath.toFile()));
    }

    private static boolean playerExists(ServerPlayerEntity player) {
        JsonObject json =

        player.getUuid()
        return false;
    }
}
