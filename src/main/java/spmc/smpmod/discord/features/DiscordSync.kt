package spmc.smpmod.discord.features

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import spmc.smpmod.SMPMod
import java.util.*

class DiscordSync: SavedData {
	constructor()
    constructor(discordMap: MutableMap<UUID, Long>) { discordMap.putAll(discordMap) }

	val discordMap: MutableMap<UUID, Long>field: BiMap<UUID, Long> = HashBiMap.create()
	fun getUUID(id: Long): UUID? = discordMap.inverse()[id]
	fun playerLinked(id: UUID) = discordMap.containsKey(id)
	fun memberLinked(id: Long) = discordMap.containsValue(id)
	fun getMember(uuid: UUID): Long? = discordMap[uuid]

	fun registerNpc(uuid: UUID, id: Long) {
		discordMap[uuid] = id
		this.setDirty()
	}

	companion object {
		val CODEC: Codec<DiscordSync> = RecordCodecBuilder.create { it.group(Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).optionalFieldOf("uuid", mapOf()).forGetter(DiscordSync::discordMap)).apply(it, ::DiscordSync)}
		val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "discord"), ::DiscordSync, CODEC, DataFixTypes.PLAYER)
		fun get() = SMPMod.minecraftServer?.dataStorage?.computeIfAbsent(TYPE)
	}
}