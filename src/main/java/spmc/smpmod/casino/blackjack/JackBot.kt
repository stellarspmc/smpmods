package spmc.smpmod.casino.blackjack

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import spmc.smpmod.npc.NPCManager

object JackBot {
	fun spawnBot(level: ServerLevel, pos: BlockPos) {
		val index = 1
		NPCManager.spawn("jack$index", level, pos)
		TODO("stub")
	}
}
