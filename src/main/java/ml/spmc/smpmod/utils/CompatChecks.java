package ml.spmc.smpmod.utils;

import net.fabricmc.loader.api.FabricLoader;

import static ml.spmc.smpmod.SMPMod.modLogger;

public class CompatChecks {
    public static void checkAndAnnounce() {
        if (FabricLoader.getInstance().isModLoaded("cardboard") || FabricLoader.getInstance().isModLoaded("banner"))
            modLogger.error(
                    """
                            Cardboard/Banner detected! This mod doesn't work with it!
                            You won't get any support as long as it's present!
                            Read more at: https://gist.github.com/Patbox/e44844294c358b614d347d369b0fc3bf""");
    }
}
