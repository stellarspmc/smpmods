package spmc.smpmod.vault

import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable
import spmc.smpmod.vault.entries.ActivePerk
import spmc.smpmod.vault.entries.ConfiguredEvent

enum class VaultTier(@JvmField val costGoal: Double, @JvmField val perks: MutableList<ActivePerk>, @JvmField val eventPool: MutableList<ConfiguredEvent>): StringRepresentable {
	ALPHA(125000.0, mutableListOf(
		ActivePerk(ActivePerk.PerkType.BONUS_SHARPNESS, 1),
		ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 1),
		ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 1)
		// ActivePerk(ActivePerk.PerkType.EXP_MULTIPLIER, 1)
	), mutableListOf(
		ConfiguredEvent(ConfiguredEvent.EventType.TREASURE_RARITY_LUCK, 1.0),
		ConfiguredEvent(ConfiguredEvent.EventType.HASTE_BUFF, 1.0),
		ConfiguredEvent(ConfiguredEvent.EventType.DEPOSIT_MONEY_BOOST, .05),
		ConfiguredEvent(ConfiguredEvent.EventType.EXTENDED_EFFECT_DURATION, .25)
	)),
	BETA(225000.0, mutableListOf(
		ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 2),
		ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 2),
		ActivePerk(ActivePerk.PerkType.BONUS_PROTECTION, 1)
		// ActivePerk(ActivePerk.PerkType.MOVEMENT_SPEED, 1)
	), mutableListOf(
		ConfiguredEvent(ConfiguredEvent.EventType.RESISTANCE_BUFF, 1.0),
		ConfiguredEvent(ConfiguredEvent.EventType.BLOCK_TREASURE_RATE, .1)
	)),
	DELTA(345000.0, mutableListOf(
		ActivePerk(ActivePerk.PerkType.BONUS_SHARPNESS, 2),
		ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 3),
		ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 3),
		ActivePerk(ActivePerk.PerkType.BONUS_PROTECTION, 2)
		// ActivePerk(ActivePerk.PerkType.EXP_MULTIPLIER, 2),
		// ActivePerk(ActivePerk.PerkType.GLOBAL_LUCK, 1)
	), mutableListOf(
		ConfiguredEvent(ConfiguredEvent.EventType.TREASURE_ALWAYS_RARE, 1.0),
		ConfiguredEvent(ConfiguredEvent.EventType.BLOCK_TREASURE_RATE, .15),
		ConfiguredEvent(ConfiguredEvent.EventType.DEPOSIT_MONEY_BOOST, .03),
		ConfiguredEvent(ConfiguredEvent.EventType.LUCK_EFFECT, 3.0),
		ConfiguredEvent(ConfiguredEvent.EventType.HASTE_BUFF, 2.0)
	)),
	GAMMA(575000.0, mutableListOf(
			ActivePerk(ActivePerk.PerkType.BONUS_SHARPNESS, 3),
		ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 4),
		ActivePerk(ActivePerk.PerkType.BONUS_PROTECTION, 3),
		ActivePerk(ActivePerk.PerkType.GATHERING_INCOME, 1)
		// ActivePerk(ActivePerk.PerkType.FAST_SMELTING, 1),
		// ActivePerk(ActivePerk.PerkType.EXP_MULTIPLIER, 3)
	), mutableListOf(
		ConfiguredEvent(ConfiguredEvent.EventType.BLOCK_TREASURE_RATE, .2),
		ConfiguredEvent(ConfiguredEvent.EventType.RPG_MOB_DROP_LUCK, 1.5),
		ConfiguredEvent(ConfiguredEvent.EventType.MACHINE_SPEED_BOOST, 2.0),
		ConfiguredEvent(ConfiguredEvent.EventType.PLANT_BUFFY_DISCOUNT, .25),
		ConfiguredEvent(ConfiguredEvent.EventType.RESISTANCE_BUFF, 2.0),
		ConfiguredEvent(ConfiguredEvent.EventType.DEPOSIT_MONEY_BOOST, .06)
	));

	val nextTier: VaultTier get() {
		val values: Array<VaultTier> = entries.toTypedArray()
		val nextOrdinal = this.ordinal + 1
		return if (nextOrdinal < values.size) values[nextOrdinal] else this
	}

	override fun getSerializedName() = this.name
	companion object { @JvmField val CODEC: Codec<VaultTier> = StringRepresentable.fromEnum { entries.toTypedArray() }}
}