package spmc.smpmod.economy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.minecraft.ChatFormatting
import net.minecraft.core.UUIDUtil
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import spmc.smpmod.SMPMod
import java.util.*
import kotlin.math.min

class EconomyData @JvmOverloads constructor(balances: MutableMap<UUID, Double> = HashMap<UUID, Double>(), names: MutableMap<UUID, String> = HashMap<UUID, String>()): SavedData() {
	private val balances = HashMap(balances)
	private val names = HashMap(names)

	fun registerPlayer(uuid: UUID, name: String) {
		if (!balances.containsKey(uuid)) {
			balances[uuid] = .0
			names[uuid] = name
			this.setDirty()
		}
	}

	fun resolveName(uuid: UUID): String = names.getOrDefault(uuid, uuid.toString().substring(0, 8))
	fun getBalance(uuid: UUID): Double = balances.getOrDefault(uuid, .0)

	fun setBalance(uuid: UUID, money: Double) {
		if (money >= 0 && money < Double.MAX_VALUE) {
			balances[uuid] = money
			this.setDirty()
		}
	}

	fun changeBalance(uuid: UUID, money: Double): Boolean {
		val current = getBalance(uuid)
		if (current + money >= 0 && current + money < Double.MAX_VALUE) {
			balances[uuid] = current + money
			this.setDirty()
			return true
		}
		return false
	}

	fun top(page: Int): String {
		val sorted = this.sortedBalances
		val filtered = sorted.filter { entry -> resolveName(entry.key) != "spmc" }
		val rankings = StringBuilder()
		val pageSize = 10
		val startIndex = (page - 1) * pageSize
		val endIndex = min(startIndex + pageSize, filtered.size)

		if (startIndex >= filtered.size || startIndex < 0) return "*No data available for this page.*"
		for (i in startIndex ..< endIndex) {
			val entry = filtered[i]
			val name = MarkdownSanitizer.escape(resolveName(entry.key))

			rankings.append(String.format("`#%02d` **%s** • $%,.2f\n", i + 1, name, entry.value))
		}

		return rankings.toString()
	}

	fun getMinecraftTop(page: Int): Component {
		val sorted = this.sortedBalances
		val filtered = sorted.filter { entry -> resolveName(entry.key) != "spmc" }
		val pageSize = 10
		val startIndex = (page - 1) * pageSize
		val endIndex = min(startIndex + pageSize, filtered.size)

		if (startIndex >= filtered.size || startIndex < 0) return Component.literal("No data available for this page.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)

		val rankings = Component.empty()
		for (i in startIndex ..< endIndex) {
			val entry = filtered[i]
			val name = resolveName(entry.key)
			val line = Component.literal(String.format("#%02d ", i + 1)).withStyle(ChatFormatting.GRAY).append(Component.literal(name).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).append(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal(String.format("$%,.2f", entry.value)).withStyle(ChatFormatting.GREEN))

			rankings.append(line)
			if (i < endIndex - 1) rankings.append("\n")
		}

		return rankings
	}

	val sortedBalances: List<Map.Entry<UUID, Double>> = balances.entries.sortedBy { it.value }.asReversed()

	companion object {
		private val BALANCES_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.DOUBLE)
		private val NAMES_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING)

		val CODEC: Codec<EconomyData> = RecordCodecBuilder.create { instance -> instance.group(BALANCES_CODEC.fieldOf("balances").forGetter { data -> data.balances }, NAMES_CODEC.fieldOf("names").forGetter { data -> data.names }).apply(instance) { balances, names -> EconomyData(balances, names) } }
		val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "economy"), { EconomyData() }, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)

		@JvmStatic
		fun get(): EconomyData? = SMPMod.minecraftServer?.overworld()?.dataStorage?.computeIfAbsent(TYPE)

	}
}