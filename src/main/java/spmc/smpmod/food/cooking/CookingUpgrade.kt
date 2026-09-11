package spmc.smpmod.food.cooking

class CookingUpgrade {

	// TODO: think how it works -> t1 to t5 (upgrade furnace / smoke / blast? (no))
	fun getLevel(rank: Int): Double {
		return when(rank) {
			1 -> 1.0
			2 -> 2.0
			3 -> 3.0
			else -> .0
		} // formula? or predefined numbers? TODO: think
	}


}