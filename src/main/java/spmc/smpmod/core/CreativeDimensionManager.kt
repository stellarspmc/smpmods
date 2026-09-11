package spmc.smpmod.core

import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.GameType
import spmc.smpmod.SMPMod.Companion.minecraftServer

object CreativeDimensionManager {

	fun teleportToCreative(player: ServerPlayer) {
		val creativeWorld = minecraftServer?.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("smpmod", "creative"))) ?: return

		swapInventories(player, toCreative = true)
		player.setGameMode(GameType.CREATIVE)
		//player.teleport(TeleportTransition(creativeWorld, spawnPos, 0.0f, 0.0f)) save the coords
	}

	fun teleportToOverworld(player: ServerPlayer) {
		val overworld = minecraftServer?.overworld() ?: return

		swapInventories(player, toCreative = false)
		player.setGameMode(GameType.SURVIVAL)
		//player.teleport(TeleportTransition(overworld, spawnPos, 0.0f, 0.0f)) save the coords TODO
	}

	private fun swapInventories(player: ServerPlayer, toCreative: Boolean) {
		val nbt = player.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
		val modData = nbt.getCompoundOrEmpty("ModData")
		val registryAccess = player.registryAccess()

		val currentInvList = ListTag()
		/*for (slot in 0 until player.inventory.containerSize) {
			val stack = player.inventory.getItem(slot)
			if (!stack.isEmpty) {
				val itemTag = CompoundTag()
				itemTag.putByte("Slot", slot.toByte())
				//stack.save(registryAccess, itemTag)
				currentInvList.add(itemTag)
			}
		}

		val saveKey = if (toCreative) "SurvivalInventory" else "CreativeInventory"
		val loadKey = if (toCreative) "CreativeInventory" else "SurvivalInventory"

		modData.put(saveKey, currentInvList)
		player.inventory.clearContent()

		if (modData.contains(loadKey)) {
			val loadInvList = modData.getListOrEmpty(loadKey)
			for (i in loadInvList.indices) {
				val itemTag = loadInvList.getCompoundOrEmpty(i)
				val slot = itemTag.getByte("Slot")
				val stack = ItemStack(registryAccess, itemTag).orElse(ItemStack.EMPTY)

				if (slot in 0 until player.inventory.containerSize) {
					player.inventory.setItem(slot, stack)
				}
			}
		}

		nbt.put("ModData", modData)
		player.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt))
		player.inventory.setChanged()
		player.inventoryMenu.broadcastChanges()
		player.containerMenu.sendAllDataToRemote()*/
		TODO("not yet done")
	}
}