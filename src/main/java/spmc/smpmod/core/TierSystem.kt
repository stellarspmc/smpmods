package spmc.smpmod.core

enum class TierSystem(vararg val type: String) {
	T1("wood", "stone", "cactus"),
	T2("copper", "gold", "bronze"),
	T3("iron", "redstone", "lapis", "electrum"),
	T4("emerald", "ferrosilicon"),
	T5("diamond", "steel", "platinum"),
	T6("netherite", "void", "adamantite", "sculk"),
	T7("elemental", "chromatic", "celestial"),
	T8("astral");
	// TODO
	// if stats unified, whats the point?

	fun getColor() = ItemRarity.entries[this.ordinal].color
}
