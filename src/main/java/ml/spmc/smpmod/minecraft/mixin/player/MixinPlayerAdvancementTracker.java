package ml.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.spmc.smpmod.SMPMod.MESSAGECHANNEL;

@Mixin(PlayerAdvancementTracker.class)
public class MixinPlayerAdvancementTracker {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(value = "TAIL"))
    private void addMessage(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.getDisplay() == null) return;
        String advancementName = advancement.getDisplay().getTitle().getString();
        String sent = "Nice, " + owner.getName().getString() + " has done " + advancementName;
        switch (advancement.getDisplay().getFrame()) {
            case TASK -> sent = "Nice, " + owner.getName().getString() + " has done " + advancementName;
            case GOAL -> sent = "Nice, " + owner.getName().getString() + " has achieved " + advancementName;
            case CHALLENGE -> sent = "Nice, " + owner.getName().getString() + " has finished " + advancementName;
        }
        MESSAGECHANNEL.sendMessage(MarkdownSanitizer.escape(sent)).queue();
    }
}
