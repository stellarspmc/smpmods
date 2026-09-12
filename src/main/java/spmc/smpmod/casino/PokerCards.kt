package spmc.smpmod.casino

enum class PokerCards {
	TWO_S, // TODO: assign map to them
	TWO_H,
	TWO_C,
	TWO_T,
	ACE_S,
	ACE_H;

	enum class Numbers {
		TWO, ACE, KING, QUEEN, JESTER, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE
	}

	enum class Suits {
		SPADES, HEARTS, CLUBS, DIAMONDS
	}
}