package spmc.smpmod.vault.entries

import net.minecraft.server.level.ServerLevel

interface VaultEntry {
	fun id(): String
	fun value(): Double
	fun apply(level: ServerLevel)
}