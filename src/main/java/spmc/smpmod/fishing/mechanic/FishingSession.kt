package spmc.smpmod.fishing.mechanic

import spmc.smpmod.fishing.RodTiers
import spmc.smpmod.utils.MessageUtils.sendError
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FishingHook

class FishingSession(private val player: ServerPlayer, private val hook: FishingHook, private val tier: RodTiers) {
    private var cursor = 0.0f
    private var movingRight = true
    private var wasJumping: Boolean
    private var ticksLeft = 100
    private var streak = 0

    private val greenStart: Float
    private val greenEnd: Float

    init {
        val zoneWidth: Float = tier.greenZoneSize
        this.greenStart = 0.5f - (zoneWidth / 2.0f)
        this.greenEnd = 0.5f + (zoneWidth / 2.0f)
        this.wasJumping = player.isJumping
    }

    fun tick(): Boolean {
        if (!player.isAlive || hook.isRemoved || ticksLeft-- <= 0) {
            onFail("Time ran out!")
            player.sendSystemMessage(Component.empty(), true)
            return true
        }

        val speed = 0.05f
        if (movingRight) {
            cursor += speed
            if (cursor >= 1.0f) {
                cursor = 1.0f
                movingRight = false
            }
        } else {
            cursor -= speed
            if (cursor <= 0.0f) {
                cursor = 0.0f
                movingRight = true
            }
        }

        player.sendSystemMessage(buildActionBarComponent(), true)
        val isJumping = player.lastClientInput.jump()
        if (isJumping && !wasJumping) {
            val hit = (cursor in greenStart..greenEnd)
            if (hit) onSuccess()
            else onFail("Missed the timing!")
            return true
        }

        this.wasJumping = isJumping
        return false
    }

    private fun buildActionBarComponent(): Component {
        val bar = Component.literal("Reel in! [ ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)

        val totalSegments = 24
        val cursorPos = (cursor * totalSegments).toInt()
        val gStartPos = (greenStart * totalSegments).toInt()
        val gEndPos = (greenEnd * totalSegments).toInt()

        for (i in 0..totalSegments) {
            when (i) {
                cursorPos -> bar.append(Component.literal("┃").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                in gStartPos..gEndPos -> bar.append(Component.literal("▒").withStyle(ChatFormatting.GREEN))
                else -> bar.append(Component.literal("─").withStyle(ChatFormatting.DARK_GRAY))
            }
        }

        return bar.append(Component.literal(" ] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal("Jump").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
    }

    private fun onSuccess() {
        if (streak > 0) streak++
        FishingLoot.rewardFish(player, tier, streak)
        hook.discard()
    }

    private fun onFail(reason: String) {
        streak = 0
        sendError(player, reason, 0)
        hook.discard()
    }
}
