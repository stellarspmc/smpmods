package spmc.smpmod.minigame.wordle

import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.fishing.mechanic.FishingLoot
import spmc.smpmod.utils.MessageUtils.sendError

class WordleSession(private val player: ServerPlayer) {
	private val word: String = WordleWords.entries[player.random.nextInt(WordleWords.entries.size)].name // TODO: think -> different word / day || player?

	fun tick() {

	}

	private fun onSuccess() {
		// TODO: reward player
	}

	private fun onFail(reason: String) {
		sendError(player, reason, 0)
	}
}
