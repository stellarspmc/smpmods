package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.utils.MarkdownParser;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerPlayNetworkHandler {

    @Final
    @Shadow private ServerPlayer player;

    @Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;)V", at = @At("HEAD"))
    private void handleMessage(ServerboundChatPacket packet, CallbackInfo ci) {
        SMPMod.sendWebhookMessage(MarkdownParser.parseMarkdown(packet.message()), player.getName().getString(), player.getStringUUID());
    }

}
