package ml.spmc.smpmod.utils;

import ml.spmc.smpmod.utils.UtilClass;
import net.fabricmc.loader.api.FabricLoader;

public class CompatChecks {
    public static void checkAndAnnounce() {
        if (FabricLoader.getInstance().isModLoaded("cardboard") || FabricLoader.getInstance().isModLoaded("banner"))
            UtilClass.errorLog(
                    "Cardboard/Banner detected! This mod doesn't work with it!\n" +
                    "You won't get any support as long as it's present!\n" +
                    "Read more at: https://gist.github.com/Patbox/e44844294c358b614d347d369b0fc3bf");
    }
}
