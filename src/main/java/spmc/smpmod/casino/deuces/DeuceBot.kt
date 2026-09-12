package spmc.smpmod.casino.deuces

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import spmc.smpmod.npc.NPCManager

object DeuceBot {
	fun spawnBot(level: ServerLevel, pos: BlockPos) {
		val index = 1
		NPCManager.spawn("deuce$index", level, pos)
		TODO("stub")
	}
}
