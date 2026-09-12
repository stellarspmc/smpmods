package spmc.smpmod.vault.entries

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import spmc.smpmod.SMPMod
import spmc.smpmod.npc.NPCData.Companion.get
import java.util.function.*

@JvmRecord
data class ActivePerk(val type: PerkType, val level: Int): VaultEntry {
	override fun id() = type.serializedName
	override fun value() = level.toDouble()
	override fun apply(level: ServerLevel) { if ((get()?: return).getMannequin((SMPMod.minecraftServer?: return).overworld(), "vault_guardian") != null) SMPMod.minecraftServer!!.playerList.players.forEach(( { type.trigger(this@ActivePerk.level, it) })) }

	override fun toString(): String {
		val formattedName = StringBuilder()
		for (word in type.serializedName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) if (word.isNotEmpty()) formattedName.append(word[0].uppercaseChar()).append(word.substring(1)).append(" ")
		return formattedName.toString().trim { it <= ' ' } + " " + toRomanNumeral(level)
	}

	enum class PerkType(private val applyCallback: BiConsumer<Int, ServerPlayer>): StringRepresentable {
		EXTRA_HEARTS({ tierLevel, player ->
			player.getAttribute(Attributes.MAX_HEALTH)?.addOrReplacePermanentModifier(AttributeModifier(Identifier.fromNamespaceAndPath("smpmod", "perk_extra_hearts"), tierLevel * 2.0, AttributeModifier.Operation.ADD_VALUE))
		}),
		BONUS_SHARPNESS({ tierLevel, player ->
			player.getAttribute(Attributes.ATTACK_DAMAGE)?.addOrReplacePermanentModifier(AttributeModifier(Identifier.fromNamespaceAndPath("smpmod", "perk_bonus_sharpness"), tierLevel * 1.25, AttributeModifier.Operation.ADD_VALUE))
		}),
		BONUS_EFFICIENCY({ tierLevel, player ->
			player.getAttribute(Attributes.MINING_EFFICIENCY)?.addOrReplacePermanentModifier(AttributeModifier(Identifier.fromNamespaceAndPath("smpmod", "perk_bonus_efficiency"), tierLevel * 2.0, AttributeModifier.Operation.ADD_VALUE))
		}),
		BONUS_PROTECTION({ tierLevel, player ->
			player.getAttribute(Attributes.ARMOR)?.addOrReplacePermanentModifier(AttributeModifier(Identifier.fromNamespaceAndPath("smpmod", "perk_bonus_protection"), tierLevel * 2.0, AttributeModifier.Operation.ADD_VALUE))
		}),
		GATHERING_INCOME({ _, _ -> TODO() });

		fun trigger(tierLevel: Int, player: ServerPlayer) { applyCallback.accept(tierLevel, player) }
		override fun getSerializedName() = this.name
		companion object { val CODEC: Codec<PerkType> = StringRepresentable.fromEnum { entries.toTypedArray() } }
	}

	companion object {
		val CODEC: Codec<ActivePerk> = RecordCodecBuilder.create { it.group(PerkType.CODEC.fieldOf("type").forGetter(ActivePerk::type), Codec.INT.fieldOf("level").forGetter(ActivePerk::level)).apply(it, ::ActivePerk) }
		private fun toRomanNumeral(level: Int): String { // TODO: above level 5
			return when (level) {
				1 -> "I"
				2 -> "II"
				3 -> "III"
				4 -> "IV"
				5 -> "V"
				else -> "Lvl $level"
			}
		}
	}
}
