package spmc.smpmod.fishing

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.core.UUIDUtil
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import spmc.smpmod.SMPMod
import spmc.smpmod.registry.FishingRegistry.allFish
import spmc.smpmod.registry.FishingRegistry.getCategoryFromFish
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class FishTracker @JvmOverloads constructor(fishUnlocked: MutableMap<UUID, MutableList<String>> = HashMap()): SavedData() {
	val unlocked: MutableMap<UUID, MutableList<String>> = HashMap()
	init { fishUnlocked.forEach { (uuid: UUID, list: MutableList<String>) -> this.unlocked[uuid] = ArrayList(list) }}

	fun getUnlockedFish(id: UUID) = unlocked.getOrDefault(id, ArrayList())
	fun addFish(id: UUID, fish: String) {
		val list = unlocked.computeIfAbsent(id) { _ -> ArrayList() }
		if (!list.contains(fish)) {
			list.add(fish)
			this.setDirty()
		}
	}

	companion object {
		private val UNLOCKED_CODEC: Codec<MutableMap<UUID, MutableList<String>>> = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.list(Codec.STRING))

		val CODEC: Codec<FishTracker> = RecordCodecBuilder.create { it.group(UNLOCKED_CODEC.fieldOf("unlocked").forGetter(FishTracker::unlocked)).apply(it, ::FishTracker) }
		val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "fish_tracker"), { FishTracker() }, CODEC, DataFixTypes.PLAYER)
		@JvmStatic fun get(): FishTracker? = SMPMod.minecraftServer?.overworld()?.dataStorage?.computeIfAbsent(TYPE)

		fun openFishIndexMenu(player: ServerPlayer): Int {
			openFishIndexMenu(player, 0)
			return 1
		}

		fun openFishIndexMenu(player: ServerPlayer, page: Int) {
			val maxPages = max(1, ceil(allFish.size.toDouble() / 45).toInt())
			val currentPage = Math.clamp(page.toLong(), 0, maxPages - 1)

			val gui = SimpleGui(MenuType.GENERIC_9x6, player, false)
			gui.setTitle(Component.literal("Fish Codex (" + (currentPage + 1) + "/" + maxPages + ")"))

			refreshGui(gui, player, currentPage, maxPages)
			gui.open()
		}

		private fun refreshGui(gui: SimpleGui, player: ServerPlayer, page: Int, maxPages: Int) {
			val unlockedList = get()?.getUnlockedFish(player.getUUID()) ?: return

			val startIndex = page * 45
			val endIndex = min(startIndex + 45, allFish.size)
			for (i in 0 .. 44) {
				val fishIndex = startIndex + i

				if (fishIndex < endIndex) {
					val fishItem = allFish[fishIndex]
					val fishId = BuiltInRegistries.ITEM.getKey(fishItem).path

					val isUnlocked = unlockedList.contains(fishId)
					if (isUnlocked) gui.setSlot(i, GuiElementBuilder(fishItem).addLoreLine(Component.literal("✔ Unlocked").withColor(TextColor.fromRgb(0x55FF55))).addLoreLine(Component.literal("Price: ").withStyle(ChatFormatting.GRAY).append(Component.literal("$" + FishItem.getModifiedPrice(fishItem.defaultInstance)).withStyle(ChatFormatting.GREEN)).withStyle { it.withItalic(false) }))
					else gui.setSlot(i, GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("???").withColor(TextColor.fromRgb(0xAAAAAA))).addLoreLine(Component.literal(String.format("Found in: %s", getCategoryFromFish(fishItem).toString())).withColor(TextColor.fromRgb(0xFF5555))))
				} else gui.setSlot(i, GuiElementBuilder(Items.AIR))
			}

			for (slot in 45 .. 53) gui.setSlot(slot, GuiElementBuilder(Items.STAINED_GLASS_PANE.lightGray()).setName(Component.literal("")))
			if (page > 0) gui.setSlot(45, GuiElementBuilder(Items.ARROW).setName(Component.literal("← Previous Page").withColor(TextColor.fromRgb(0xFFFF55))).setCallback { _ -> openFishIndexMenu(player, page - 1) })
			gui.setSlot(49, GuiElementBuilder(Items.PAPER).setName(Component.literal("Page " + (page + 1) + " of " + maxPages).withColor(TextColor.fromRgb(0xFFFFFF))).addLoreLine(Component.literal("Unlocked: " + unlockedList.size + " / " + allFish.size).withColor(TextColor.fromRgb(0xAAFFAA))))
			if (page < maxPages - 1) gui.setSlot(53, GuiElementBuilder(Items.ARROW).setName(Component.literal("Next Page →").withColor(TextColor.fromRgb(0xFFFF55))).setCallback { _ -> openFishIndexMenu(player, page + 1) })
		}
	}
}