package spmc.smpmod.core

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.ChatFormatting
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import spmc.smpmod.SMPMod
import java.util.*
import java.util.function.BiConsumer

class NPCData: SavedData {
    constructor()
    constructor(npcs: MutableMap<String, UUID>) { npcMap.putAll(npcs) }

    val npcMap: MutableMap<String, UUID>field: BiMap<String, UUID> = HashBiMap.create()
    fun removeNpc(id: String) { if (npcMap.remove(id) != null) this.setDirty() }
    fun getUuid(id: String): UUID? = npcMap[id]
    fun hasNpc(id: String): Boolean = npcMap.containsKey(id)
    fun getNpcId(uuid: UUID): String? = npcMap.inverse()[uuid]

    fun registerNpc(id: String, uuid: UUID) {
        npcMap[id] = uuid
        this.setDirty()
    }

    fun getMannequin(level: ServerLevel, id: String): Mannequin? = level.getEntity(getUuid(id) ?: return null) as? Mannequin
    companion object {
        val CODEC: Codec<NPCData> = RecordCodecBuilder.create { it.group(Codec.unboundedMap(Codec.STRING, UUIDUtil.CODEC).optionalFieldOf("npcs", mapOf()).forGetter(NPCData::npcMap)).apply(it, ::NPCData)}
	    val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "npc_data"), ::NPCData, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)
	    @JvmStatic fun get() = SMPMod.minecraftServer?.overworld()?.dataStorage?.computeIfAbsent(TYPE) // TODO: change for dimensions
    }
}

@Suppress("UsePropertyAccessSyntax")
object NPCManager {
	private val DEFINITIONS: MutableMap<String, CustomNPC> = HashMap()

	@JvmStatic fun register(npc: CustomNPC) { DEFINITIONS[npc.id] = npc }
	fun getDefinition(id: String): CustomNPC? = DEFINITIONS[id]
	fun isRegistered(id: String): Boolean = DEFINITIONS.containsKey(id)

	@JvmStatic val allIds: List<String> get() = DEFINITIONS.keys.toList()

	@JvmStatic
	fun spawn(id: String, level: ServerLevel, pos: BlockPos): Mannequin? {
		val def = DEFINITIONS[id] ?: return null

		val data = NPCData.get() ?: return null
		if (data.hasNpc(id)) return data.getMannequin(level, id)

		val mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED) ?: return null
		mannequin.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
		if (def.profile != null) mannequin.profile = def.profile
		mannequin.customName = def.displayName
		mannequin.setImmovable(true)
		mannequin.isInvulnerable = true
		mannequin.setHideDescription(true)

		level.addFreshEntity(mannequin)
		data.registerNpc(id, mannequin.getUUID())
		return mannequin
	}

	@JvmStatic
	fun register() {
		AttackEntityCallback.EVENT.register { player, world, hand, entity, _ ->
			if (hand != InteractionHand.MAIN_HAND || world.isClientSide) return@register InteractionResult.PASS
			if (entity is Mannequin) {
				(DEFINITIONS[NPCData.get()?.getNpcId(entity.getUUID()) ?: return@register InteractionResult.PASS] ?: return@register InteractionResult.PASS).onAttack.accept(player as ServerPlayer, entity)
				return@register InteractionResult.SUCCESS
			}
			return@register InteractionResult.PASS
		}

		UseEntityCallback.EVENT.register { player, world, hand, entity, _ ->
			if (world.isClientSide || hand != InteractionHand.MAIN_HAND) return@register InteractionResult.PASS
			if (entity is Mannequin) {
				(DEFINITIONS[NPCData.get()?.getNpcId(entity.getUUID()) ?: return@register InteractionResult.PASS] ?: return@register InteractionResult.PASS).onUse.accept(player as ServerPlayer, entity)
				return@register InteractionResult.SUCCESS
			}
			return@register InteractionResult.PASS
		}
	}

	@JvmStatic
	fun serverTickLoop(server: MinecraftServer) {
		val npcData = NPCData.get()?: return
		for (uuid in npcData.npcMap.values) {
			val def = DEFINITIONS[npcData.getNpcId(uuid)]
			val entity = server.overworld().getEntity(uuid) // TODO: account of different dimensions
			if (entity is Mannequin && entity.isAlive && def != null && def.lookAtPlayer()) {
				val nearestPlayer = entity.level().getNearestPlayer(entity, 12.0) ?: return
				entity.lookAt(EntityAnchorArgument.Anchor.EYES, nearestPlayer.eyePosition)
			}
		}
	}
}

class CustomNPC private constructor(builder: Builder) {
	val id = builder.id
	val displayName: Component
	val profile: ResolvableProfile?
	val onAttack: BiConsumer<ServerPlayer, Mannequin>
	val onUse: BiConsumer<ServerPlayer, Mannequin>
	private val lookAtPlayer: Boolean

	init {
		this.displayName = builder.displayName
		this.profile = builder.profile
		this.onAttack = builder.onAttack
		this.onUse = builder.onUse
		this.lookAtPlayer = builder.lookAtPlayer
	}

	fun lookAtPlayer() = lookAtPlayer

	class Builder(var id: String, lookAtPlayer: Boolean) { // TODO: could make better
		var displayName: Component
		var profile: ResolvableProfile? = null
		val lookAtPlayer: Boolean
		var onAttack: BiConsumer<ServerPlayer, Mannequin> = { _, _ -> }
		var onUse: BiConsumer<ServerPlayer, Mannequin> = { _, _ -> }

		init {
			this.displayName = Component.literal(id)
			this.lookAtPlayer = lookAtPlayer
		}

		fun displayName(displayName: Component) = apply { this.displayName = displayName }
		fun profile(profile: ResolvableProfile) = apply { this.profile = profile }
		fun skin(name: String, uuidIntArray: IntArray, textureValue: String) = apply { this.profile = createCustomProfile(name, uuidIntArray, textureValue) }
		fun onAttack(onAttack: BiConsumer<ServerPlayer, Mannequin>) = apply { this.onAttack = onAttack }
		fun onUse(onUse: BiConsumer<ServerPlayer, Mannequin>) = apply { this.onUse = onUse }

		fun build() = CustomNPC(this)
	}
}

fun createCustomProfile(name: String, uuidIntArray: IntArray, textureValue: String) = createCustomProfile(name, UUIDUtil.uuidFromIntArray(uuidIntArray), textureValue)
fun talkAsMannequin(mannequin: Mannequin, message: Component, player: ServerPlayer) { player.sendSystemMessage(Component.empty().append(mannequin.customName ?: mannequin.name).append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(message.copy().withStyle(ChatFormatting.WHITE))) }
fun createCustomProfile(name: String, uuid: UUID, textureValue: String): ResolvableProfile {
	val map: Multimap<String, Property> = HashMultimap.create()
	map.put("textures", Property("textures", textureValue))

	return ResolvableProfile.createResolved(GameProfile(uuid, name, PropertyMap(map)))
}