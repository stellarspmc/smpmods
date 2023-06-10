package ml.spmc.smpmod.minecraft.mixin;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.spmc.smpmod.SMPMod.MESSAGECHANNEL;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        MESSAGECHANNEL.sendMessage(MarkdownSanitizer.escape("☠ " + ((PlayerEntity) (Object) this).getName().getString() + " died in the " + ((PlayerEntity) (Object) this).getWorld().getDimension().toString() + "at " + ((PlayerEntity) (Object) this).getX() + ", " + ((PlayerEntity) (Object) this).getY() + ", " + ((PlayerEntity) (Object) this).getZ())).queue();
        /* TODO: fix this code
        double rand = new Random().nextDouble();
        if (rand > 0.9) {
            UtilClass.getDatabaseManager().changeBalance(plr.getName().getString(), rand);
            plr.sendSystemMessage(Component.literal("You lost " + rand + " coins!").withStyle(ChatFormatting.DARK_RED));
        } if (rand > 0.99999983) UtilClass.getDatabaseManager().changeBalance(plr.getName().getString(), 1);*/
    }
}
