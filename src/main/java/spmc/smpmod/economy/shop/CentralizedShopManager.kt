package spmc.smpmod.economy.shop

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import java.util.UUID
import kotlin.math.min

object CentralizedShopManager {
	val simpleMemberTracker: MutableMap<UUID, Double> = mutableMapOf()// TODO: yuu ($1 -> 1 pt, 100pt -> $1)

	fun organizeShopsAsInventory(player: ServerPlayer) {// TODO: websho
		val level = player.level()
		val shopList = ShopManager.getAllShopsByLevel(level)
		//val gui = SimpleGui(MenuType.GENERIC_9x6, player, false)

		//refreshGui(gui, player, 1, shopList)
		//gui.open()
	}

	private fun refreshGui(gui: SimpleGui, player: ServerPlayer, page: Int, shopList: List<ShopData>) {
		val maxPage = (shopList.size / 45)

		val startIndex = page * 45
		val endIndex = min(startIndex + 45, shopList.size)
		for (i in 0 .. 44) {
			val index = startIndex + i

			if (index < endIndex) {

				gui.setSlot(i, GuiElementBuilder(createShopItem(shopList[i])).setCallback { it -> callback(it, shopList[i], player) })
			} else gui.setSlot(i, GuiElementBuilder(Items.AIR))
		}

		for (slot in 45 .. 53) gui.setSlot(slot, GuiElementBuilder(Items.STAINED_GLASS_PANE.lightGray()).setName(Component.literal("")))
		if (page > 0) gui.setSlot(45, GuiElementBuilder(Items.ARROW).setName(Component.literal("← Previous Page").withColor(TextColor.fromRgb(0xFFFF55))).setCallback { _ -> refreshGui(gui, player, page - 1, shopList) })
		gui.setSlot(49, GuiElementBuilder(Items.PAPER).setName(Component.literal("Page ${page + 1} of $maxPage").withColor(TextColor.fromRgb(0xFFFFFF))).addLoreLine(Component.literal("Shops: ${shopList.size}").withColor(TextColor.fromRgb(0xAAFFAA))))
		if (page < maxPage - 1) gui.setSlot(53, GuiElementBuilder(Items.ARROW).setName(Component.literal("Next Page →").withColor(TextColor.fromRgb(0xFFFF55))).setCallback { _ -> refreshGui(gui, player, page + 1, shopList) })
	}

	private fun createShopItem(data: ShopData): ItemStack {
		val item = data.getItemSold().copy()
		item.set(DataComponents.CUSTOM_NAME, Component.literal("${data.getStack()}x $item"))
		item.set(DataComponents.LORE, ItemLore(listOf(
			Component.literal("Selling For: $${data.getPrice()}"), Component.literal("Stock left: ${data.availableStock}")
		)))
		// TODO: shop displays
		// data to show: position, selling item, stock, price...
		// present method: gui
		return item
	}

	private fun callback(clickType: ClickType, data: ShopData, player: ServerPlayer) {
		if (clickType.isLeft) {
			if (!data.isOwner(player)) data.processPurchase(player)
			else data.openOwnerMenu(player)
		} else if (clickType.isRight && data.isOwner(player)) {
			data.barrelPos // TODO: open the barrel lol
		}
	}
}
