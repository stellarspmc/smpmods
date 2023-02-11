package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.minecraft.callback.ServerChatCallback;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.commons.lang3.StringUtils;
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
        String string = packet.getMessage();
        String msg = StringUtils.normalizeSpace(string);
        ServerChatCallback.EVENT.invoker().onServerChat(player, msg);
    }

}
