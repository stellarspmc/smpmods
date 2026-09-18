package spmc.smpmod.minigame

import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.utils.*

object WordleManager: SessionManager<WordleSession>() {
	fun startMinigame(player: ServerPlayer): Boolean {
		if (anyInCreative(player)) return false
		val session = WordleSession(player)
		return startSession(session)
	}
}

class WordleSession(val player: ServerPlayer): GameSession(listOf(player)) {
	private val word: String = WordleWords.entries[player.random.nextInt(WordleWords.entries.size)].name // TODO: think -> different word / day || player?

	override fun tick(): Boolean {
		TODO("implement input word mechanic...")
	}

	override fun onEnd(reason: SessionEndReason) {
		when (reason) {
			SessionEndReason.SUCCESS -> TODO("reward player")
			else -> sendError(player, message = reason.name)
		}
	}
}

enum class WordleWords {
	ENEMY;
}

private enum class WordleMaps {
	// TODO: add the maps
}