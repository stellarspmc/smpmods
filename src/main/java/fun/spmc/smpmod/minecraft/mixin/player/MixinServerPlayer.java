package fun.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fun.spmc.smpmod.SMPMod.messageChannel;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {
    @Inject(method = "die", at = @At(value = "TAIL"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        String deathMessage = source.getLocalizedDeathMessage(player).getString();
        String fullMessage = "☠ " + deathMessage + " (at " + (int)player.getX() + ", " + (int)player.getY() + ", " + (int)player.getZ() + ")";

        messageChannel.sendMessage(MarkdownSanitizer.escape(fullMessage)).queue();
    }
}
