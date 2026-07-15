package fun.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fun.spmc.smpmod.SMPMod.messageChannel;
import static fun.spmc.smpmod.SMPMod.minecraftServer;

@Mixin(PlayerList.class)
public class MixinPlayerManager {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        if (minecraftServer == null) minecraftServer = server;
        messageChannel.sendMessage("[+] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayer player, CallbackInfo ci) {
        messageChannel.sendMessage("[-] " + MarkdownSanitizer.escape(player.getName().getString())).queue();
    }
}
