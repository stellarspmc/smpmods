package `fun`.spmc.smpmod.npc

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import `fun`.spmc.smpmod.SMPMod
import net.minecraft.ChatFormatting
import net.minecraft.core.UUIDUtil
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.util.*
import java.util.function.Function

class NPCData : SavedData {
    constructor()
    constructor(npcs: MutableMap<String, UUID>) { npcMap.putAll(npcs) }

    val npcMap: MutableMap<String, UUID>field: BiMap<String, UUID> = HashBiMap.create<String, UUID>()
    fun removeNpc(id: String) { if (npcMap.remove(id) != null) this.setDirty() }
    fun getUuid(id: String): UUID? { return npcMap[id] }
    fun hasNpc(id: String): Boolean { return npcMap.containsKey(id) }
    fun getNpcId(uuid: UUID): String? { return npcMap.inverse()[uuid] }

    fun registerNpc(id: String, uuid: UUID) {
        npcMap[id] = uuid
        this.setDirty()
    }

    fun getMannequin(level: ServerLevel, id: String): Mannequin? {
        val uuid = getUuid(id) ?: return null

        val entity = level.getEntity(uuid)
        if (entity is Mannequin) return entity
        return null
    }

    companion object {
        val CODEC: Codec<NPCData> = RecordCodecBuilder.create(Function { instance: RecordCodecBuilder.Instance<NPCData> -> instance.group(Codec.unboundedMap(Codec.STRING, UUIDUtil.CODEC).optionalFieldOf("npcs", mapOf()).forGetter { obj: NPCData -> obj.npcMap }).apply(instance) { npcs: MutableMap<String, UUID> -> NPCData(npcs) } })
        val TYPE: SavedDataType<NPCData> = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "npc_data"), { NPCData() }, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)

        @JvmStatic fun get(): NPCData { return SMPMod.minecraftServer!!.overworld().dataStorage.computeIfAbsent(TYPE) }
        @JvmStatic fun createCustomProfile(name: String, uuidIntArray: IntArray, textureValue: String): ResolvableProfile { return createCustomProfile(name, UUIDUtil.uuidFromIntArray(uuidIntArray), textureValue) }
        @JvmStatic fun talkAsMannequin(mannequin: Mannequin, message: Component, player: ServerPlayer) { player.sendSystemMessage(Component.empty().append(mannequin.customName ?: mannequin.name).append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(message.copy().withStyle(ChatFormatting.WHITE))) }

        @JvmStatic
        fun createCustomProfile(name: String, uuid: UUID, textureValue: String): ResolvableProfile {
            val map: Multimap<String, Property> = HashMultimap.create()
            map.put("textures", Property("textures", textureValue))

            val properties = PropertyMap(map)
            val profile = GameProfile(uuid, name, properties)

            return ResolvableProfile.createResolved(profile)
        }
    }
}
