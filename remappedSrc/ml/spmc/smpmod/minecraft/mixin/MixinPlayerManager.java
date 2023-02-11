package ml.spmc.smpmod.minecraft.mixin;

import ml.spmc.smpmod.minecraft.callback.PlayerJoinCallback;
import ml.spmc.smpmod.minecraft.callback.PlayerLeaveCallback;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerManager {

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void onPlayerConnect(Connection connection, ServerPlayer player, CallbackInfo ci) {
        PlayerJoinCallback.EVENT.invoker().onJoin(connection, player);
    }


    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayer player, CallbackInfo ci) {
        PlayerLeaveCallback.EVENT.invoker().onLeave(player);
    }


}
