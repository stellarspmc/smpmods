package spmc.smpmod.core

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.portal.TeleportTransition
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import net.minecraft.world.phys.Vec3
import spmc.smpmod.SMPMod
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CreativeData @JvmOverloads constructor(creativeData: MutableMap<UUID, CompoundTag> = mutableMapOf(), survivalData: MutableMap<UUID, CompoundTag> = mutableMapOf()): SavedData() {
	private val creativeData = ConcurrentHashMap(creativeData)
	private val survivalData = ConcurrentHashMap(survivalData)

	companion object {
		private val INVENTORY_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, CompoundTag.CODEC)
		val CODEC: Codec<CreativeData> = RecordCodecBuilder.create{ it.group(INVENTORY_CODEC.fieldOf("c_inv").forGetter(CreativeData::creativeData), INVENTORY_CODEC.fieldOf("s_inv").forGetter(CreativeData::survivalData)).apply(it, ::CreativeData) }
		val TYPE: SavedDataType<CreativeData> = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "creative"), ::CreativeData, CODEC, DataFixTypes.LEVEL)
		@JvmStatic fun get(): CreativeData? = SMPMod.minecraftServer?.dataStorage?.computeIfAbsent(TYPE)

		fun teleportToCreative(player: ServerPlayer) {
			swapInventoriesAndLocation(player, true)
		}

		fun teleportToOverworld(player: ServerPlayer) {
			swapInventoriesAndLocation(player, false)
		}

		private fun swapInventoriesAndLocation(player: ServerPlayer, toCreative: Boolean) {
			val server = SMPMod.minecraftServer ?: return
			val state = get()!!
			val levelTo = if (toCreative) server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("smpmod", "creative"))) ?: return else server.overworld()

			// save data
			val saveData = (if (toCreative) state.survivalData else state.creativeData).computeIfAbsent(player.uuid) { CompoundTag() }
			saveData.store("pos", Vec3.CODEC, player.position())
			player.inventory.forEachIndexed { index, stack -> saveData.store("slot$index", ItemStack.CODEC, stack) }
			state.setDirty()

			// load other data
			val loadData = (if (toCreative) state.creativeData else state.survivalData).computeIfAbsent(player.uuid) { CompoundTag() }
			player.inventory.clearContent()
			for (i in 0..player.inventory.containerSize) {
				val stack = loadData.read("slot$i", ItemStack.CODEC)
				if (stack.isPresent) player.inventory.setItem(i, stack.get())
			}

			var backupPos: Vec3 = Vec3.ZERO
			if (!toCreative) {
				val sp = player.respawnConfig?.respawnData?: levelTo.respawnData
				if (sp.dimension() == Level.OVERWORLD) backupPos = blockPos2Vec3(sp.pos())
			}
			val pos = if (loadData.read("pos", Vec3.CODEC).isPresent) loadData.read("pos", Vec3.CODEC).get() else backupPos

			player.inventory.setChanged()
			player.inventoryMenu.broadcastChanges()
			player.containerMenu.sendAllDataToRemote()

			player.setGameMode(if (toCreative) GameType.CREATIVE else GameType.SURVIVAL)
			player.teleport(TeleportTransition(levelTo, pos, Vec3.ZERO, 0f, 0f, TeleportTransition.DO_NOTHING))
		}

		private fun blockPos2Vec3(pos: BlockPos) = Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
	}
}
