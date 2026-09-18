package spmc.smpmod.core

import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3
import spmc.smpmod.SMPMod.Companion.minecraftServer
import java.util.*
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.sin
import kotlin.math.sqrt

class PetData(type: PetType, xp: Int): SavedData() {

	// TODO

	companion object {
		val CODEC: Any = TODO()
	}
}

object PetManager {
	private val petTickMap: MutableMap<UUID, Short> = mutableMapOf()
	private val petMap: MutableMap<UUID, ArmorStand> = mutableMapOf()
	//val CODEC: Any = TODO()
	//val TYPE: SavedDataType<PetManager> = SavedDataType(id("pets") ::PetManager, CODEC, DataFixTypes.PLAYER)
	//fun get() = minecraftServer?.dataStorage?.get(TYPE)

	fun spawnPet(player: ServerPlayer) { // ran when joined
		if (petTickMap.contains(player.uuid) || petMap.contains(player.uuid)) return
		val pet = EntityTypes.ARMOR_STAND.create(player.level(), EntitySpawnReason.TRIGGERED) ?: return
		pet.isInvisible = true
		pet.setNoBasePlate(true)
		pet.isPermanentlyInvulnerable = true

		val head = ItemStack(Items.PLAYER_HEAD)
		head.applyComponents(DataComponentMap.builder().set(DataComponents.PROFILE, createCustomProfile("pet_${player.scoreboardName}", UUID.randomUUID(), "")).build()) // todo
		pet.setItemSlot(EquipmentSlot.HEAD, head)

		pet.customName = Component.literal("") // todo
		pet.setPos(player.position())
		player.level().addFreshEntity(pet)
	}

	fun petLoop() {
		minecraftServer?.playerList?.players?.forEach {
			if (!petMap.contains(it.uuid)) return@forEach
			if (!it.isAlive) petMap[it.uuid]?.setPos(Vec3(.0, 1000.0, .0))
			val tick = petTickMap.getOrDefault(it.uuid, 0)
			val limit = 20
			val x = tick / 20 * 1.5 - .75
			val y = abs(sin(tick / limit * Math.PI) * .25) + .25
			val z = if (x > limit/2) sqrt(abs(x)) else -sqrt(abs(x))
			petMap[it.uuid]?.setPos(Vec3(x, y, z))
			petTickMap[it.uuid] = Math.clamp((tick + 1).toLong(), 0, 20).toShort()
		}
	}

	fun despawnPet(player: ServerPlayer) {
		(petMap[player.uuid]?: return).discard()
		petMap.remove(player.uuid)
	}
}

enum class PetType(base64: String): StringRepresentable {
	;
	companion object { val CODEC = StringRepresentable.fromEnum { PetType.entries.toTypedArray() } }
}

fun getLevel(xp: Int) = (log(xp / 100f, 2f).coerceIn(0f, 100f)).toInt()