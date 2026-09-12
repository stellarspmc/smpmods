package spmc.smpmod.vault

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.sgui.api.gui.AnvilInputGui
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.ChatFormatting
import net.minecraft.core.UUIDUtil
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.EconomySystem
import spmc.smpmod.npc.NPCData
import spmc.smpmod.utils.MessageUtils.sendError
import spmc.smpmod.vault.entries.ActivePerk
import spmc.smpmod.vault.entries.ConfiguredEvent
import spmc.smpmod.vault.entries.VaultEntry
import java.util.*
import java.util.function.Consumer
import kotlin.math.max

class VaultData: SavedData {
	var currentMoney = .0
		private set
	var currentTier = VaultTier.ALPHA
		private set
	val activePerks = ArrayList<ActivePerk>()
	val activeEvents = ArrayList<ConfiguredEvent>()
	val leaderboard: MutableMap<UUID, Double> = mutableMapOf()

	constructor()

	constructor(currentTier: VaultTier, currentMoney: Double, perks: MutableList<ActivePerk>, events: MutableList<ConfiguredEvent>, leaderboard: MutableMap<UUID, Double>) {
		this.currentTier = currentTier
		this.currentMoney = currentMoney
		this.activePerks.addAll(perks)
		this.activeEvents.addAll(events)
		this.leaderboard.putAll(leaderboard)
	}

	fun recordDonation(playerUuid: UUID, amount: Double) {
		this.leaderboard.merge(playerUuid, amount) { a, b -> java.lang.Double.sum(a, b) }
		this.addMoney(amount)
	}

	private fun addMoney(amount: Double) {
		this.currentMoney += amount

		var unlockedSomething = false
		while (canAdvance()) {
			currentMoney = max(0.0, currentMoney - currentTier.costGoal)
			advance()
			unlockedSomething = true
		}

		if (unlockedSomething || amount > 0) setDirty()
	}

	private fun canAdvance(): Boolean {
		val availableEvents = this.availableEvents
		val availablePerks = this.availablePerks
		val check = this.currentMoney >= currentTier.costGoal

		if (availableEvents.isEmpty() && availablePerks.isEmpty()) return (this.currentTier != VaultTier.GAMMA) && check
		return check
	}

	private fun advance() {
		val level = SMPMod.minecraftServer?.overworld() ?: return
		val random = level.getRandom()

		val availableEvents = this.availableEvents
		val availablePerks = this.availablePerks

		if (availableEvents.isEmpty() && availablePerks.isEmpty()) {
			if (this.currentTier != VaultTier.GAMMA) this.currentTier = this.currentTier.nextTier
			return
		}

		var pickEvent = random.nextBoolean()
		if (pickEvent && availableEvents.isEmpty()) pickEvent = false
		if (!pickEvent && availablePerks.isEmpty()) pickEvent = true

		val entry: VaultEntry
		if (pickEvent) {
			entry = availableEvents[random.nextInt(availableEvents.size)]
			this.activeEvents.add(entry)
		} else {
			entry = availablePerks[random.nextInt(availablePerks.size)]

			this.activePerks.removeIf { it.type == entry.type }
			this.activePerks.add(entry)
		}
		entry.apply(level)
	}

	private val availableEvents: MutableList<ConfiguredEvent> get() = currentTier.eventPool.filter { !this.activeEvents.contains(it) }.toMutableList()
	private val availablePerks: MutableList<ActivePerk> get() = currentTier.perks.filter { this.activePerks.none { a -> a.type == it.type && a.level >= it.level }}.toMutableList()
	internal val topDonors get() = this.leaderboard.entries.sortedBy { it.value }.asReversed().take(5)

	class DonateAnvilGui(player: ServerPlayer?): AnvilInputGui(player, false) {
		init {
			this.setTitle(Component.literal("Vault Donation"))
			this.setDefaultInputValue("10")
			this.updateOutputSlot()
		}

		override fun onInput(input: String?) {
			this.updateOutputSlot()
		}

		private fun updateOutputSlot() {
			val input = input.trim { it <= ' ' }
			var amount = .0
			var isValid = false

			try {
				amount = input.toDouble()
				if (amount > 0 && (EconomySystem.get()?: return).getBalance(player.getUUID()) >= amount) isValid = true
			} catch (_: NumberFormatException) { }

			val outputItem: ItemStack
			if (isValid) {
				outputItem = Items.STAINED_GLASS_PANE.lime().defaultInstance
				outputItem.set(DataComponents.CUSTOM_NAME, Component.literal("Click to Donate $" + String.format("%.2f", amount)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
			} else {
				outputItem = Items.STAINED_GLASS_PANE.red().defaultInstance
				outputItem.set(DataComponents.CUSTOM_NAME, Component.literal("Enter a valid amount (> 0)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
			}

			val finalAmount = amount
			val canDonate = isValid

			this.setSlot(2, outputItem, Consumer {
				if (!canDonate) return@Consumer
				val vaultData: VaultData = (get()?: return@Consumer)
				if ((EconomySystem.get() ?: return@Consumer).changeBalance(player.getUUID(), -finalAmount)) {
					vaultData.recordDonation(player.getUUID(), finalAmount)
					player.sendSystemMessage(Component.literal("Thank you! You donated ").withStyle(ChatFormatting.GREEN).append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.GOLD)).append(Component.literal(" to the Vault!")))
				} else sendError(player, "You do not have enough money to donate to the Vault.")
				this.close()
			})
		}
	}

	companion object {
		val CODEC: Codec<VaultData> = RecordCodecBuilder.create { it.group(VaultTier.CODEC.fieldOf("vault_tier").forGetter(VaultData::currentTier), Codec.DOUBLE.fieldOf("current_money").forGetter(VaultData::currentMoney), Codec.list(ActivePerk.CODEC).fieldOf("perks").forGetter(VaultData::activePerks), Codec.list(ConfiguredEvent.CODEC).fieldOf("events").forGetter(VaultData::activeEvents), Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.DOUBLE).optionalFieldOf("leaderboard", mapOf()).forGetter(VaultData::leaderboard)).apply(it, ::VaultData) }
		val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "vault"), ::VaultData, CODEC, DataFixTypes.LEVEL)
		@JvmField var buffValue: Float = 0f

		fun get(): VaultData? = SMPMod.minecraftServer?.dataStorage?.computeIfAbsent(TYPE)


		fun register() {
			ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
				for ((type, level) in (get() ?: return@register).activePerks) if (NPCData.get()?.getMannequin((SMPMod.minecraftServer ?: return@register).overworld(), "vault_guardian") != null) type.trigger(level, handler.getPlayer())
			}

			ServerPlayerEvents.AFTER_RESPAWN.register { _, player, _ ->
				for ((type, level) in (get() ?: return@register).activePerks) if (NPCData.get()?.getMannequin((SMPMod.minecraftServer ?: return@register).overworld(), "vault_guardian") != null) type.trigger(level, player)
			}

			ServerTickEvents.END_SERVER_TICK.register {
				val vault = (get() ?: return@register)
				if (vault.activeEvents.isNotEmpty() && vault.activeEvents.removeIf { a -> a.tick(it.overworld()) }) vault.setDirty()
			}
		}

		fun sendVaultMessage(player: ServerPlayer): Int {
			player.sendSystemMessage(Component.literal("Vault Status").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
			player.sendSystemMessage(Component.literal("Tier: ").withStyle(ChatFormatting.YELLOW).append(Component.literal((get()?: return -1).currentTier.name).withStyle(ChatFormatting.GREEN)))
			player.sendSystemMessage(Component.literal("Balance: ").withStyle(ChatFormatting.YELLOW).append(Component.literal(String.format("$%.2f / $%.2f", get()!!.currentMoney, get()!!.currentTier.costGoal)).withStyle(ChatFormatting.AQUA)))
			player.sendSystemMessage(Component.literal("Top Donors:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
			val topDonors = get()!!.topDonors.toMutableList()
			if (topDonors.isEmpty()) player.sendSystemMessage(Component.literal("  - No donations yet").withStyle(ChatFormatting.GRAY))
			else {
				var rank = 1
				for ((key, value) in topDonors) player.sendSystemMessage(Component.literal(String.format("  #%d %s: ", rank++, EconomySystem.get()!!.resolveName(key))).withStyle(ChatFormatting.YELLOW).append(Component.literal(String.format("$%.2f", value)).withStyle(ChatFormatting.GREEN)))
			}

			player.sendSystemMessage(Component.literal("Active Perks:").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
			if (get()!!.activePerks.isEmpty()) player.sendSystemMessage(Component.literal("  - None").withStyle(ChatFormatting.GRAY))
			else for (perk in get()!!.activePerks) player.sendSystemMessage(Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(perk.toString()).withStyle(ChatFormatting.GRAY)))
			player.sendSystemMessage(Component.literal("Active Events:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
			if (get()!!.activeEvents.isEmpty()) player.sendSystemMessage(Component.literal("  - None").withStyle(ChatFormatting.GRAY))
			else for (event in get()!!.activeEvents) player.sendSystemMessage(Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(event.toString()).withStyle(ChatFormatting.GRAY)))

			return 1
		}
	}
}