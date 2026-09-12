package spmc.smpmod.events

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import spmc.smpmod.SMPMod.Companion.minecraftServer
import spmc.smpmod.utils.UtilFunc.getY
import java.util.Locale

object ServerEvents {
	fun spawnEvent() { // only in overworld!
		val server = minecraftServer ?: return
		val pos = getLocation(server)
		val event = EventType.entries[server.overworld().random.nextInt(EventType.entries.size)]

		// set a 30x30 border for the event
		server.sendSystemMessage(Component.literal("A $event is happening at (${pos.x}, ${pos.z})!"))
	}

	private fun getLocation(server: MinecraftServer): BlockPos {
		val x = randomPosition(server)
		val z = randomPosition(server)
		return BlockPos(x, getY(x, z, server.overworld()), z)
	}

	private fun randomPosition(server: MinecraftServer): Int {
		val avoidedZone = -300 .. 600 // TODO: check
		var x = server.overworld().random.nextIntBetweenInclusive(-5000, 5000)
		while (x in avoidedZone) x = server.overworld().random.nextIntBetweenInclusive(-5000, 5000)
		return x
	}

	enum class EventType {
		BARREL_DROP,
		MOB_FIGHT,
		MINI_BOSS_FIGHT, // TODO
		METEORITE; // TODO

		override fun toString() = name[0].toString() + name.substring(1).lowercase(Locale.getDefault()) // TODO: should do smt about this?
	}
}