package spmc.smpmod.core

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.item.component.CustomData
import spmc.smpmod.economy.EconomySystem
import spmc.smpmod.utils.rnd2DP
import spmc.smpmod.utils.sendError
import spmc.smpmod.utils.sendSuccess
import java.util.*

object BountySystem {
	private val killCooldowns: MutableMap<Pair<UUID, UUID>, Long> = mutableMapOf()

	fun executeVictim(player: ServerPlayer, damageSource: DamageSource) {
		val eco = EconomySystem.get() ?: return
		val victimBalance = eco.getBalance(player.getUUID())
		if (victimBalance >= 1000) {
			val lossPercent = .05 + (player.getRandom().nextDouble() * .05)
			val totalLost = rnd2DP(victimBalance * lossPercent)

			if (totalLost > 0) {
				eco.changeBalance(player.getUUID(), -totalLost)
				sendError<Int>(player, message = String.format("You died and lost $%.2f (%.1f%% of your balance)!", totalLost, lossPercent * 100))
				if (damageSource.entity?.getUUID() != player.getUUID()) changeBounty(player, damageSource.entity as? ServerPlayer?: return, totalLost)
			}
		}
	}

	private fun changeBounty(victim: ServerPlayer, killer: ServerPlayer, lost: Double) {
		val eco = EconomySystem.get() ?: return
		val activeBounty = checkBounty(victim)
		val now = System.currentTimeMillis()
		val pair = Pair(killer.uuid, victim.uuid)
		val lastKillTime = killCooldowns.getOrDefault(pair, 0L)
		val isFarming = (now - lastKillTime) < 5 * 60 * 1000L

		killCooldowns[pair] = now
		if (isFarming) {
			sendError<Int>(killer, message = "You killed ${victim.scoreboardName} too recently! No bounty or cash awarded.")
			return
		}

		var totalReward = .0
		if (activeBounty > .0) {
			totalReward += activeBounty
			clearBounty(victim)
			killer.sendSystemMessage(Component.literal(String.format("⚔ You claimed a $%.2f bounty placed on %s!", activeBounty, victim.scoreboardName)).withStyle(ChatFormatting.GOLD))
		}

		if (lost > .0) totalReward += rnd2DP(lost * 0.9)
		if (totalReward > .0) {
			eco.changeBalance(killer.uuid, totalReward)
			sendSuccess<Int>(killer, message = String.format("Total payout received: $%.2f", totalReward))
			killer.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update { it.putDouble("bounty", it.getDoubleOr("bounty", 250.0) * 1.1) }
		}
	}

	fun addPlayerBounty(adder: ServerPlayer, victim: ServerPlayer, bounty: Double, anonymous: Boolean): Int {
		val eco = EconomySystem.get() ?: return -1
		if (eco.changeBalance(adder.getUUID(), -rnd2DP(bounty))) {
			victim.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update {
				val originalBounty = it.getDoubleOr("bounty", .0)
				it.putDouble("bounty", originalBounty + bounty)
				victim.sendSystemMessage(Component.literal("${if (anonymous) "Someone" else adder.scoreboardName} has added a bounty of ${rnd2DP(bounty)} on you!"), false)
			}
			return sendSuccess(adder, message = "Added a bounty of ${rnd2DP(bounty)} to ${victim.scoreboardName}.")
		} else return sendError(adder, message = "Insufficient funds! You need ${rnd2DP(bounty)}.")
	}

	fun checkBounty(player: ServerPlayer) = player.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("bounty", .0)
	private fun clearBounty(player: ServerPlayer) { player.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update { it.remove("bounty") } }
}
