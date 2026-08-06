package fun.spmc.smpmod.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fun.spmc.smpmod.SMPMod.messageChannel;

@Pseudo
@Mixin(targets = "com.fibermc.essentialcommands.playerdata.PlayerData", remap = false)
public class MixinPlayerData {
    @Shadow private ServerPlayer player;

    @Inject(method = "setAfk", at = @At("TAIL"))
    private void onSetAfkTail(boolean afk, CallbackInfo ci) {
        if (this.player != null) {
            if (afk) messageChannel.sendMessage(String.format("%s is now AFK.", player.getScoreboardName())).queue();
            else messageChannel.sendMessage(String.format("%s is no longer AFK.", player.getScoreboardName())).queue();
        }
    }
}
