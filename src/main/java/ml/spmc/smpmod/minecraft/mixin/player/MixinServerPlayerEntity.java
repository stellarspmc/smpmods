package ml.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.spmc.smpmod.SMPMod.messageChannel;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
    @Inject(method = "onDeath", at = @At(value = "TAIL"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        messageChannel.sendMessage(MarkdownSanitizer.escape("☠ " + ((PlayerEntity) (Object) this).getName().getString() + " died in " + ((PlayerEntity) (Object) this).getX() + ", " + ((PlayerEntity) (Object) this).getY() + ", " + ((PlayerEntity) (Object) this).getZ())).queue();
    }
}
