package `fun`.spmc.smpmod.treasure

import `fun`.spmc.smpmod.core.ItemRarity
import `fun`.spmc.smpmod.treasure.ChunkPool.checkChunkPool
import `fun`.spmc.smpmod.treasure.ChunkPool.increment
import `fun`.spmc.smpmod.treasure.TreasureHelper.rollTreasureRarity
import `fun`.spmc.smpmod.treasure.TreasureSpawner.spawnTreasureContainer
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.state.BlockState
import java.util.*

object TreasureEvents {
    @JvmField var eventPercentage: Double = 1.0

    fun onBlockBreak(world: Level, player: Player, pos: BlockPos, state: BlockState) {
        if (world.isClientSide || player !is ServerPlayer) return

        val mainHand = player.mainHandItem
        val enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val silkTouchHolder: Optional<Holder.Reference<Enchantment>> = enchantmentRegistry.get(Enchantments.SILK_TOUCH)
        if (silkTouchHolder.isPresent && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), mainHand) > 0) return

        val biomes = TreasureHelper.Biomes.getGroup(world.registryAccess().lookupOrThrow(Registries.BIOME).getResourceKey(world.getBiome(pos).value()).orElse(Biomes.PLAINS)!!)
        val rarity: ItemRarity = rollTreasureRarity(state, eventPercentage, world.getRandom()) ?: return

        if (checkChunkPool(ChunkPos.containing(pos))) return
        increment(ChunkPos.containing(pos))
        spawnTreasureContainer(world as ServerLevel, pos, rarity, player, biomes)
    }
}
