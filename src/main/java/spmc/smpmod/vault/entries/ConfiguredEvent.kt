package spmc.smpmod.vault.entries

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.fluctuate.FluctuationData.Companion.changeMargin
import spmc.smpmod.npc.NPCData.Companion.get
import spmc.smpmod.treasure.TreasureHelper
import spmc.smpmod.vault.VaultData
import java.util.function.BiConsumer
import java.util.function.Consumer

class ConfiguredEvent @JvmOverloads constructor(val type: EventType, val modifier: Double, remainingTicks: Int = -1): VaultEntry {
	var remainingTicks: Int
		private set

	init { this.remainingTicks = if (remainingTicks == -1) type.durationTick else remainingTicks }


	override fun id() = type.serializedName
	override fun value() = modifier
	override fun apply(level: ServerLevel) { if ((get()?: return).getMannequin((SMPMod.minecraftServer?: return).overworld(), "vault_guardian") != null) type.trigger(modifier, level) }

	fun tick(level: ServerLevel): Boolean {
		if ((get()?: return false).getMannequin((SMPMod.minecraftServer?: return false).overworld(), "vault_guardian") != null) {
			this.remainingTicks--
			if (this.remainingTicks % 20 == 0) type.triggerTick(modifier, level)
			if (this.remainingTicks <= 0) type.triggerEnd(level)
			return this.remainingTicks <= 0
		}
		return false
	}

	override fun toString(): String {
		val formattedName = StringBuilder()
		for (word in type.serializedName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) if (word.isNotEmpty()) formattedName.append(word[0].uppercaseChar()).append(word.substring(1)).append(" ")

		val timeFormatted = formatTime(remainingTicks)
		if (modifier > 1) return String.format("%s (+%.0f%%) - %s", formattedName.toString().trim { it <= ' ' }, (modifier - 1.0) * 100, timeFormatted)
		return String.format("%s - %s", formattedName.toString().trim { it <= ' ' }, timeFormatted)
	}

	private fun formatTime(ticks: Int): String {
		if (ticks <= 0) return "Expired"

		val totalSeconds = ticks / 20
		val hours = totalSeconds / 3600
		val minutes = (totalSeconds % 3600) / 60
		val seconds = totalSeconds % 60

		return if (hours > 0) String.format("%dh %dm", hours, minutes)
		else String.format("%02d:%02d", minutes, seconds)
	}

	enum class EventType // start and tick same func
	constructor(val durationTick: Int, private val startCallback: BiConsumer<Double, ServerLevel>, private val tickCallback: BiConsumer<Double, ServerLevel> = startCallback, private val endCallback: Consumer<ServerLevel> = Consumer { _ -> }): StringRepresentable {
		TREASURE_RARITY_LUCK(1440 * 20 * 60, { _, _ -> }),
		HASTE_BUFF(720 * 20 * 60, { amplifier, level -> level.server.playerList.players.forEach(( { applyEffects(it, MobEffects.HASTE, amplifier.toInt()) })) }),
		DEPOSIT_MONEY_BOOST(60 * 20 * 60, { boostPercent, _ -> changeMargin(1 - boostPercent) }, { changeMargin(1.0) }),
		RESISTANCE_BUFF(720 * 20 * 60, { amplifier, level -> level.server.playerList.players.forEach(( { applyEffects(it, MobEffects.RESISTANCE, amplifier.toInt()) })) }),
		BLOCK_TREASURE_RATE(120 * 20 * 60, { rateBonus, _ -> TreasureHelper.eventPercentage = 1 + rateBonus }, { TreasureHelper.eventPercentage = 1.0 }),
		TREASURE_ALWAYS_RARE(15 * 20 * 60, { _, _ -> TreasureHelper.rigTreasures = true }, { TreasureHelper.rigTreasures = false }),
		EXTENDED_EFFECT_DURATION(120 * 20 * 60, { amplifier, _ -> VaultData.buffValue = amplifier.toFloat() }, { VaultData.buffValue = 0f }),
		LUCK_EFFECT(120 * 20 * 60, { amplifier, level -> level.server.playerList.players.forEach(( { applyEffects(it, MobEffects.LUCK, amplifier.toInt()) })) }),
		RPG_MOB_DROP_LUCK(180 * 20 * 60, { _, _ -> TODO() }),
		MACHINE_SPEED_BOOST(120 * 20 * 60, { _, _ -> TODO() }),
		PLANT_BUFFY_DISCOUNT(360 * 20 * 60, { _, _ -> TODO() });

		// start, end
		constructor(durationTick: Int, startCallback: BiConsumer<Double, ServerLevel>, endCallback: Consumer<ServerLevel>): this(durationTick, startCallback, { _, _ -> }, endCallback)

		fun trigger(modifierValue: Double, level: ServerLevel) { startCallback.accept(modifierValue, level) }
		fun triggerTick(modifier: Double, level: ServerLevel) { tickCallback.accept(modifier, level) }
		fun triggerEnd(level: ServerLevel) { endCallback.accept(level) }
		override fun getSerializedName() = this.name


		companion object {
			val CODEC: Codec<EventType> = StringRepresentable.fromEnum { entries.toTypedArray() }
			private fun applyEffects(player: ServerPlayer, effect: Holder<MobEffect>, amp: Int) { player.addEffect(MobEffectInstance(effect, 40, amp - 1, false, false, true)) }
		}
	}

	companion object {
		val CODEC: Codec<ConfiguredEvent> = RecordCodecBuilder.create { it.group(EventType.CODEC.fieldOf("type").forGetter(ConfiguredEvent::type), Codec.DOUBLE.fieldOf("modifier").forGetter(ConfiguredEvent::modifier), Codec.INT.optionalFieldOf("remaining_ticks", -1).forGetter<ConfiguredEvent?>(ConfiguredEvent::remainingTicks)).apply(it, ::ConfiguredEvent) }
	}
}