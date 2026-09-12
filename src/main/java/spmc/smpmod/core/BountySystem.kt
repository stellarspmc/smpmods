package spmc.smpmod.core

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.DoubleTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.item.component.CustomData
import spmc.smpmod.economy.EconomySystem
import spmc.smpmod.utils.MessageUtils
import spmc.smpmod.utils.UtilFunc.rnd2DP

object BountySystem {

	fun executeVictim(player: ServerPlayer, damageSource: DamageSource) {
		val eco = EconomySystem.get() ?: return
		val victimBalance = eco.getBalance(player.getUUID())
		if (victimBalance >= 1000) {
			val lossPercent = .05 + (player.getRandom().nextDouble() * .05)
			val totalLost = rnd2DP(victimBalance * lossPercent)

			if (totalLost > 0) {
				eco.changeBalance(player.getUUID(), -totalLost)
				MessageUtils.sendError<Int>(player, String.format("You died and lost $%.2f (%.1f%% of your balance)!", totalLost, lossPercent * 100))

				if (damageSource.entity?.getUUID() != player.getUUID()) changeBounty(player, damageSource.entity as? ServerPlayer?: return, totalLost)
			}
		}
	}

	private fun changeBounty(victim: ServerPlayer, killer: ServerPlayer, lost: Double) {
		val eco = EconomySystem.get() ?: return
		val bountyReward = rnd2DP(lost * .9)

		eco.changeBalance(killer.getUUID(), bountyReward)

		val victimNbt = victim.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
		MessageUtils.sendSuccess<Int>(killer, String.format("⚔ You killed %s and claimed a $%.2f bounty!", victim.scoreboardName, bountyReward))

		val killerNbt = killer.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
		val originalBounty = victimNbt.getDoubleOr("bounty", .0)
		killerNbt.put("bounty", DoubleTag.valueOf(bountyReward + originalBounty)) // TODO: change something about it, it is quite weird... (checking)
	}

	fun addPlayerBounty(adder: ServerPlayer, victim: ServerPlayer, bounty: Double, anonymous: Boolean): Int {
		val eco = EconomySystem.get() ?: return -1
		if (eco.changeBalance(adder.getUUID(), -rnd2DP(bounty))) {
			val victimNbt = victim.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
			val originalBounty = victimNbt.getDoubleOr("bounty", .0)
			victimNbt.put("bounty", DoubleTag.valueOf(originalBounty + bounty))
			victim.sendSystemMessage(Component.literal("${if (anonymous) "Someone" else adder.scoreboardName} has added a bounty of ${rnd2DP(bounty)} on you!"), false)
			return MessageUtils.sendSuccess(adder, "Added a bounty of ${rnd2DP(bounty)} to ${victim.scoreboardName}.")
		} else return MessageUtils.sendError(adder, "Insufficient funds! You need ${rnd2DP(bounty)}.")
	}

	fun checkBounty(player: ServerPlayer): Double = player.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("bounty", .0)
}
