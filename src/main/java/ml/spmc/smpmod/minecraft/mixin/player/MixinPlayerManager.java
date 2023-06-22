package ml.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

import static ml.spmc.smpmod.SMPMod.MESSAGECHANNEL;
import static ml.spmc.smpmod.SMPMod.SERVER;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (SERVER == null) SERVER = player.server;
        //cache.put(player.getUuid(), System.nanoTime());
        //if (!UtilClass.getDatabaseManager().playerExists(player.getUuidAsString()))
        //    UtilClass.getDatabaseManager().addPlayer(player.getUuidAsString(), player.getName().getString(), player.getIp());
        MESSAGECHANNEL.sendMessage("[+] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
    }

    private static HashMap<UUID, Long> cache = new HashMap<>();

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayerEntity player, CallbackInfo ci) {
        //UtilClass.getDatabaseManager().updatePlaytime(player.getUuidAsString(), (UtilClass.getDatabaseManager().getPlaytime(player.getUuidAsString()) + (System.nanoTime() - cache.get(player.getUuid())))/1000000);
        MESSAGECHANNEL.sendMessage("[-] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
        //UtilClass.getDatabaseManager().updateLastEntered(player.getUuidAsString());
    }


}
