package ml.spmc.smpmod.minecraft.mixin;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.spmc.smpmod.SMPMod.MESSAGECHANNEL;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements {
    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void addMessage(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {
        String advancementName = advancement.getChatComponent().getString();
        String sent = "Nice, " + player.getName().getString() + " has done " + advancementName;
        switch (advancement.getDisplay().getFrame()) {
            case TASK -> sent = "Nice, " + player.getName().getString() + " has done " + advancementName;
            case GOAL -> sent = "Nice, " + player.getName().getString() + " has achieved " + advancementName;
            case CHALLENGE -> sent = "Nice, " + player.getName().getString() + " has finished " + advancementName;
        }
        MESSAGECHANNEL.sendMessage(MarkdownSanitizer.escape(sent)).queue();
    }
}
