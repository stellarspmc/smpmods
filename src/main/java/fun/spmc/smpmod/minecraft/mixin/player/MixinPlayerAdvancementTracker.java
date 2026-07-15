package fun.spmc.smpmod.minecraft.mixin.player;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static fun.spmc.smpmod.SMPMod.messageChannel;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancementTracker {
    @Shadow private ServerPlayer player;

    @Shadow public abstract AdvancementProgress getOrStartProgress(AdvancementHolder advancement);

    @Inject(method = "award", at = @At(value = "TAIL"))
    private void addMessage(AdvancementHolder holder, String criterion, CallbackInfoReturnable<Boolean> cir) {
        if (holder.value().display().isEmpty()) return;
        if(!this.getOrStartProgress(holder).isDone()) return;
        String advancementName = holder.value().display().get().getTitle().getString();
        String sent;
        switch (holder.value().display().get().getType()) {
            case GOAL -> sent = "Nice, " + player.getName().getString() + " has achieved [" + advancementName + "]";
            case CHALLENGE -> sent = "Nice, " + player.getName().getString() + " has finished [" + advancementName + "]";
            default -> sent = "Nice, " + player.getName().getString() + " has done [" + advancementName + "]";
        }
        messageChannel.sendMessage(MarkdownSanitizer.escape(sent)).queue();
    }
}
