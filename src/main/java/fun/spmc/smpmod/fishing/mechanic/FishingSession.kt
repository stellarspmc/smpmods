package fun.spmc.smpmod.fishing.mechanic;

import fun.spmc.smpmod.fishing.RodTiers;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

public class FishingSession {
    private final ServerPlayer player;
    private final FishingHook hook;
    private final RodTiers tier;

    private float cursor = 0.0f;
    private boolean movingRight = true;
    private boolean wasJumping;
    private int ticksLeft = 100;
    private int streak = 0;

    private final float greenStart;
    private final float greenEnd;

    public FishingSession(ServerPlayer player, FishingHook hook, RodTiers tier) {
        this.player = player;
        this.hook = hook;
        this.tier = tier;

        float zoneWidth = tier.getGreenZoneSize();
        this.greenStart = 0.5f - (zoneWidth / 2.0f);
        this.greenEnd = 0.5f + (zoneWidth / 2.0f);
        this.wasJumping = player.isJumping();
    }

    public boolean tick() {
        if (!player.isAlive() || hook.isRemoved() || ticksLeft-- <= 0) {
            onFail("Time ran out!");
            player.sendSystemMessage(Component.empty(), true);
            return true;
        }

        float speed = 0.05f;
        if (movingRight) {
            cursor += speed;
            if (cursor >= 1.0f) { cursor = 1.0f; movingRight = false; }
        } else {
            cursor -= speed;
            if (cursor <= 0.0f) { cursor = 0.0f; movingRight = true; }
        }

        player.sendSystemMessage(buildActionBarComponent(), true);
        boolean isJumping = player.getLastClientInput().jump();
        if (isJumping && !wasJumping) {
            boolean hit = (cursor >= greenStart && cursor <= greenEnd);
            if (hit) onSuccess();
            else onFail("Missed the timing!");
            return true;
        }

        this.wasJumping = isJumping;
        return false;
    }

    private Component buildActionBarComponent() {
        MutableComponent bar = Component.literal("Reel in! [ ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

        int totalSegments = 24;
        int cursorPos = (int) (cursor * totalSegments);
        int gStartPos = (int) (greenStart * totalSegments);
        int gEndPos = (int) (greenEnd * totalSegments);

        for (int i = 0; i <= totalSegments; i++) {
            if (i == cursorPos) bar.append(Component.literal("┃").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
            else if (i >= gStartPos && i <= gEndPos) bar.append(Component.literal("▒").withStyle(ChatFormatting.GREEN));
            else bar.append(Component.literal("─").withStyle(ChatFormatting.DARK_GRAY));
        }

        return bar.append(Component.literal(" ] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).append(Component.literal("Jump").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
    }

    private void onSuccess() {
        if (streak > 0) streak++;
        FishingLoot.rewardFish(player, tier, streak);
        hook.discard();
    }

    private void onFail(String reason) {
        streak = 0;
        MessageUtils.sendErrorMessage(player, reason);
        hook.discard();
    }
}
