package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.utils.UtilClass;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

import static ml.spmc.smpmod.SMPMod.MESSAGECHANNEL;
import static ml.spmc.smpmod.SMPMod.SERVER;

@Mixin(PlayerList.class)
public class MixinPlayerManager {

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void onPlayerConnect(Connection connection, ServerPlayer player, CallbackInfo ci) {
        if (SERVER == null) SERVER = player.server;
        cache.put(player.getUUID(), System.nanoTime());
        if (!UtilClass.getDatabaseManager().playerExists(player.getStringUUID()))
            UtilClass.getDatabaseManager().addPlayer(player.getStringUUID(), player.getName().getString(), player.getIpAddress());
        MESSAGECHANNEL.sendMessage("[+] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
    }

    private static HashMap<UUID, Long> cache = new HashMap<>();

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayer player, CallbackInfo ci) {
        UtilClass.getDatabaseManager().updatePlaytime(player.getStringUUID(), (UtilClass.getDatabaseManager().getPlaytime(player.getStringUUID()) + (System.nanoTime() - cache.get(player.getUUID())))/1000000);
        MESSAGECHANNEL.sendMessage("[-] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
        UtilClass.getDatabaseManager().updateLastEntered(player.getStringUUID());
    }


}
