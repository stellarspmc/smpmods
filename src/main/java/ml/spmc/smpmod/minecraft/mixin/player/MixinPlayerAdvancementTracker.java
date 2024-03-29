package ml.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.advancement.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.spmc.smpmod.SMPMod.messageChannel;

@Mixin(PlayerAdvancementTracker.class)
public abstract class MixinPlayerAdvancementTracker {
    @Shadow private ServerPlayerEntity owner;

    @Shadow public abstract AdvancementProgress getProgress(Advancement advancementEntry);

    @Inject(method = "grantCriterion", at = @At(value = "TAIL"))
    private void grantCriterion(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.getDisplay() == null ||
                advancement.getDisplay().isHidden() ||
                !getProgress(advancement).isDone()) return;
        AdvancementDisplay frame = advancement.getDisplay();
        String advancementName = frame.getTitle().getString();
        String sent;
        switch (frame.getFrame()) {
            case GOAL -> sent = "Nice, " + owner.getName().getString() + " has achieved [" + advancementName + "]";
            case CHALLENGE -> sent = "Nice, " + owner.getName().getString() + " has finished [" + advancementName + "]";
            default -> sent = "Nice, " + owner.getName().getString() + " has done [" + advancementName + "]";
        }
        messageChannel.sendMessage(MarkdownSanitizer.escape(sent)).queue();
    }
}
