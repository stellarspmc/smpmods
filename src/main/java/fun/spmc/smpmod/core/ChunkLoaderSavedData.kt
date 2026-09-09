package `fun`.spmc.smpmod.core

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import `fun`.spmc.smpmod.utils.MessageUtils.sendError
import `fun`.spmc.smpmod.utils.MessageUtils.sendSuccess
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import net.minecraft.world.phys.BlockHitResult
import java.util.function.Function

class ChunkLoaderSavedData @JvmOverloads constructor(activeLoaders: MutableSet<BlockPos> = HashSet<BlockPos>()) : SavedData() {
    private val activeLoaders: MutableSet<BlockPos> = HashSet(activeLoaders)
    private var suspended = false

    fun addLoader(level: ServerLevel, pos: BlockPos) {
        if (activeLoaders.add(pos)) {
            this.setDirty()
            if (!suspended) {
                val chunkPos = ChunkPos.containing(pos)
                level.setChunkForced(chunkPos.x(), chunkPos.z(), true)
            }
        }
    }

    fun removeLoader(level: ServerLevel, pos: BlockPos) {
        if (activeLoaders.remove(pos)) {
            this.setDirty()
            val chunkPos = ChunkPos.containing(pos)
            val hasOtherLoadersInChunk = activeLoaders.stream().anyMatch { p: BlockPos -> ChunkPos.containing(p) == chunkPos }

            if (!hasOtherLoadersInChunk) level.setChunkForced(chunkPos.x(), chunkPos.z(), false)
        }
    }

    fun isLoader(pos: BlockPos): Boolean { return activeLoaders.contains(pos) }

    fun suspendAll(level: ServerLevel) {
        if (suspended) return
        this.suspended = true
        for (pos in activeLoaders) {
            val chunkPos = ChunkPos.containing(pos)
            level.setChunkForced(chunkPos.x(), chunkPos.z(), false)
        }
    }

    fun restoreAll(level: ServerLevel) {
        if (!suspended) return
        this.suspended = false
        for (pos in activeLoaders) {
            val chunkPos = ChunkPos.containing(pos)
            level.setChunkForced(chunkPos.x(), chunkPos.z(), true)
        }
    }

    companion object {
        private val LOADERS_CODEC: Codec<MutableSet<BlockPos>> = BlockPos.CODEC.listOf().xmap(
            Function { c: MutableList<BlockPos> -> HashSet(c) },
            Function { c: MutableSet<BlockPos> -> ArrayList(c) })

        val CODEC: Codec<ChunkLoaderSavedData> =
            RecordCodecBuilder.create(Function { instance: RecordCodecBuilder.Instance<ChunkLoaderSavedData> ->
                instance.group(LOADERS_CODEC.fieldOf("active_loaders").forGetter<ChunkLoaderSavedData?> { data: ChunkLoaderSavedData -> data.activeLoaders }
                ).apply(instance) { activeLoaders: MutableSet<BlockPos> -> ChunkLoaderSavedData(activeLoaders) }
            })

        val TYPE: SavedDataType<ChunkLoaderSavedData> = SavedDataType(
            Identifier.fromNamespaceAndPath("smpmod", "chunk_loaders"), { ChunkLoaderSavedData() }, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE
        )

        fun get(level: ServerLevel): ChunkLoaderSavedData {
            return level.dataStorage.computeIfAbsent(TYPE)
        }

        fun register() {
            UseBlockCallback.EVENT.register(UseBlockCallback { player: Player, level: Level, _: InteractionHand, hitResult: BlockHitResult ->
                if (level.isClientSide) return@UseBlockCallback InteractionResult.PASS
                val serverLevel = level as ServerLevel
                val pos = hitResult.blockPos

                if (level.getBlockState(pos).`is`(Blocks.LODESTONE)) {
                    val data: ChunkLoaderSavedData = get(serverLevel)

                    if (!data.isLoader(pos)) {
                        data.addLoader(serverLevel, pos)
                        return@UseBlockCallback sendSuccess(player as ServerPlayer, "Chunk loader activated.", InteractionResult.SUCCESS)
                    }
                }
                return@UseBlockCallback InteractionResult.PASS
            })

            PlayerBlockBreakEvents.BEFORE.register(PlayerBlockBreakEvents.Before { level: Level, player: Player, pos: BlockPos, state: BlockState, _: BlockEntity? ->
                if (!level.isClientSide && state.`is`(Blocks.LODESTONE)) {
                    val serverLevel = level as ServerLevel
                    val data: ChunkLoaderSavedData = get(serverLevel)

                    if (data.isLoader(pos)) {
                        data.removeLoader(serverLevel, pos)
                        return@Before sendError(player as ServerPlayer, "Chunk loader deactivated.", true)
                    }
                }
                return@Before true
            })

            ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { _: ServerGamePacketListenerImpl, _: PacketSender, server: MinecraftServer -> if (server.playerList.playerCount == 1) for (level in server.allLevels) get(level).restoreAll(level) })
            ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { _: ServerGamePacketListenerImpl, server: MinecraftServer -> if (server.playerList.playerCount <= 1) for (level in server.allLevels) get(level).suspendAll(level) })
        }
    }
}