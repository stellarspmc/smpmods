package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.utils.MarkdownParser;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow private ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void handleMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        SMPMod.sendWebhookMessage(MarkdownParser.parseMarkdown(packet.chatMessage()), player.getName().getString(), player.getUuidAsString());
    }

}
