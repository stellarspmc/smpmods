package fun.spmc.smpmod.misc;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

public class CosmeticSavedData extends SavedData {
    public static void register() {
        Placeholders.registerServer(Identifier.fromNamespaceAndPath("smpmod", "prefix"), (context, argument) -> {
            if (context.hasPlayer()) {
                Component prefix = CosmeticSavedData.getEquippedPrefix((ServerPlayer) context.player());
                if (!prefix.getString().isBlank()) return PlaceholderResult.value(prefix);
            }
            return PlaceholderResult.value(Component.empty());
        });
    }

    private static Component getEquippedPrefix(ServerPlayer player) {
        return Component.empty().append("");
    }
}
