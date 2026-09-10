package spmc.smpmod.economy.atm

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.floodgate.api.FloodgateApi
import spmc.smpmod.economy.EconomyData.Companion.get
import spmc.smpmod.economy.fluctuate.MarketState
import spmc.smpmod.utils.MessageUtils.sendError
import spmc.smpmod.utils.MessageUtils.sendSuccess
import kotlin.math.min
import kotlin.math.roundToInt

object ATMMenu {
	fun open(player: ServerPlayer) {
		if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) openBedrockForm(player)
		else openJavaGui(player)
	}

	private fun openJavaGui(player: ServerPlayer) {
		val gui = object: SimpleGui(MenuType.GENERIC_9x3, player, false) { override fun onOpen() { refreshGui(this, player) } }
		gui.setTitle(Component.literal("ATM Menu"))
		refreshGui(gui, player)
		gui.open()
	}

	private fun refreshGui(gui: SimpleGui, player: ServerPlayer) {
		val eco = get() ?: return
		for (i in 0 .. 26) gui.setSlot(i, GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" ")))
		gui.setSlot(11, GuiElementBuilder(Items.REDSTONE_BLOCK).setName(Component.literal("Withdraw $100.00").withStyle(ChatFormatting.DARK_RED).append(Component.literal(" (Right-click: Withdraw All)").withStyle(ChatFormatting.GRAY))).setCallback { type ->
			if (type.isRight) {
				val current = ((eco.getBalance(player.getUUID()) * 100f).roundToInt() / 100f).toDouble()
				if (current > 0 && current % 100 == .0) {
					eco.changeBalance(player.getUUID(), -current)
					giveExactItems(player, (current / 100).toInt())
				}
			} else {
				eco.changeBalance(player.getUUID(), -100.0)
				giveExactItems(player, 1)
			}
			refreshGui(gui, player)
		})
		val balance = eco.getBalance(player.getUUID())
		gui.setSlot(13, GuiElementBuilder(Items.GOLD_BLOCK).setName(Component.literal(String.format("Balance: $%,.2f", balance)).withStyle(ChatFormatting.GOLD)))
		gui.setSlot(15, GuiElementBuilder(Items.EMERALD_BLOCK).setName(Component.literal("Deposit All").withStyle(ChatFormatting.GREEN)).setCallback { _ ->
			eco.changeBalance(player.getUUID(), getDepositItems(player))
			refreshGui(gui, player)
		})
	}

	private fun openBedrockForm(player: ServerPlayer) {
		val eco = get() ?: return
		val balance = eco.getBalance(player.getUUID())

		val form = SimpleForm.builder().title("ATM Machine").content(String.format("Current Balance: $%,.2f", balance)).button("Withdraw $100").button("Deposit All").validResultHandler { response ->
			when (response.clickedButtonId()) {
				0 -> {
					eco.changeBalance(player.getUUID(), -100.0)
					giveExactItems(player, 1)
				}
				1 -> eco.changeBalance(player.getUUID(), getDepositItems(player))
			}
			openBedrockForm(player)
		}.build()

		FloodgateApi.getInstance().sendForm(player.getUUID(), form)
	}

	private fun giveExactItems(player: ServerPlayer, totalCount: Int) {
		var totalCount = totalCount
		val maxStack = Items.DIAMOND.defaultMaxStackSize
		while (totalCount > 0) {
			val stackSize = min(totalCount, maxStack)
			val stack = ItemStack(Items.DIAMOND, stackSize)
			if (!player.inventory.add(stack)) player.drop(stack, false)?.setNoPickUpDelay()
			totalCount -= stackSize
		}
		sendSuccess<Int>(player, String.format("Withdrew %dx Diamonds for $%d.", totalCount, totalCount * 100))
	}

	private fun getDepositItems(player: ServerPlayer): Double {
		var totalPayout = .0

		for (i in 0 ..< player.inventory.containerSize) {
			val stack = player.inventory.getItem(i)
			if (stack.isEmpty) continue

			val payout = MarketState.processItemDeposit(player, stack)
			if (payout > 0) {
				totalPayout += payout
				player.inventory.removeItem(i, stack.count)
			}
		}

		if (totalPayout > 0) sendSuccess<Int>(player, String.format("Deposited all valid items for $%.2f to your account.", totalPayout))
		sendError<Int>(player, "No valid market currency items found in inventory.")
		return totalPayout
	}
}