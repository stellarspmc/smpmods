package spmc.smpmod.pet

import spmc.smpmod.SMPMod.Companion.minecraftServer
import java.util.UUID
import kotlin.math.sin
import kotlin.math.sqrt

object PetManager {
	val petMap: MutableMap<UUID, Short> = mutableMapOf()
	val CODEC: Any = TODO()
	val TYPE: Any = TODO()

	fun get(): Any = TODO()

	fun spawnPet() {

	}

	fun petLoop() {
		minecraftServer?.playerList?.players?.forEach {
			val tick = petMap.getOrDefault(it.uuid, 0)
			val limit = 20
			val x = tick / 20 * 1.5 - .75
			val y = sin(tick / limit * Math.PI)
			val z = if (x > limit/2) sqrt(x) else sqrt(-x)
			// pets should be about .75 blocks to player, rotating at (1-5 ticks)/movement
			// 1rot/s should be fine, so petMap either 0-19 / 0-3
			// it.x/z + .75, it.x/z - .75
			// circle equation, x^2+z^2=2.25 to get points, use x from .75 to -.75
			// y: sine eqn occ from 0.25 to 0.75 -> sin(t)
			petMap[it.uuid] = Math.clamp((tick + 1).toLong(), 0, 20).toShort()
		}
	}
}
