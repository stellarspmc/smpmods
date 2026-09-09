package `fun`.spmc.smpmod.treasure

import `fun`.spmc.smpmod.SMPMod
import `fun`.spmc.smpmod.core.ItemRarity
import `fun`.spmc.smpmod.economy.EconomyData
import `fun`.spmc.smpmod.registry.TreasureRegistry
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.PowerParticleOption
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BarrelBlockEntity

object TreasureSpawner {
    @JvmStatic
    fun spawnTreasureContainer(world: ServerLevel, pos: BlockPos, rarity: ItemRarity, player: Player, biomes: TreasureHelper.Biomes) {
        world.destroyBlock(pos, true)
        world.setBlock(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 3)
        val barrel = world.getBlockEntity(pos) as? BarrelBlockEntity ?: return
        val list = TreasureRegistry.getEligibleTreasures(world, biomes, rarity)
        val availableSlots = (0 until barrel.containerSize).toMutableList()
        if (list.isEmpty()) return

        repeat(world.random.nextIntBetweenInclusive(1, 9 - rarity.ordinal)) {
            if (availableSlots.isEmpty()) return@repeat

            val treasure = list[world.random.nextInt(list.size)]
            val slotIndex = world.random.nextInt(availableSlots.size)
            barrel.setItem(availableSlots.removeAt(slotIndex), treasure.createStack())
        }

        barrel.setChanged()
        spawnLootEffects(world, pos, rarity, player)
    }

    fun spawnLootEffects(world: ServerLevel, pos: BlockPos, rarity: ItemRarity, player: Player) {
        val x = pos.x + .5
        val y = pos.y + .5
        val z = pos.z + .5

        when (rarity) {
            ItemRarity.COMMON -> {
                world.sendParticles(ParticleTypes.CRIT, x, y, z, 20, .3, .3, .3, .1)
                world.sendParticles(ParticleTypes.SMOKE, x, y, z, 10, .2, .2, .2, .02)
                world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, .8f, 1.2f)
            }

            ItemRarity.UNCOMMON -> {
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 35, .4, .4, .4, .05)
                world.sendParticles(ParticleTypes.GLOW, x, y, z, 20, .3, .3, .3, .02)
                world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1f)
                world.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, .5f, 1.5f)
            }

            ItemRarity.RARE -> TODO()

            ItemRarity.EPIC -> {
                world.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1f), x, y, z, 60, .5, .5, .5, .03)
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 25, .4, .4, .4, .08)
                world.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1f, 1f)
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, .7f, 1.3f)
            }

            ItemRarity.LEGENDARY -> {
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 120, .6, .6, .6, .3)
                world.sendParticles(ParticleTypes.FIREWORK, x, y, z, 40, .4, .4, .4, .15)
                world.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1f, 1f)
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1f, 1f)

                announceLoot(world, rarity, ChatFormatting.GOLD, player)
            }

            ItemRarity.MYTHIC -> {
                world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, -0xaa01), x, y, z, 2, 0.0, 0.0, 0.0, 0.0)
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 200, .8, .8, .8, .5)
                world.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 80, .5, .5, .5, .2)
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 60, .5, .5, .5, .1)

                world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, .7f, 1.5f)
                world.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1f, .8f)
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1f, .9f)

                announceLoot(world, rarity, ChatFormatting.LIGHT_PURPLE, player)
            }

            ItemRarity.CHROMATIC -> TODO()
            ItemRarity.ASTRAL -> TODO()
        }
    }

    private fun announceLoot(world: ServerLevel, rarity: ItemRarity, color: ChatFormatting, player: Player) {
        val eco: EconomyData = EconomyData.get()
        val balance: Double = eco.getBalance(player.getUUID())
        val balanceScale = if (balance <= 0) 1.0 else Math.clamp(1000 / balance, 0.0, 1.0)
        eco.changeBalance(player.getUUID(), 3 * balanceScale)

        val chatAnnouncement: Component = Component.literal("★ ")
            .withStyle(color, ChatFormatting.BOLD)
            .append(Component.literal(player.scoreboardName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .append(Component.literal(" found a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(rarity.name + " Drop").withStyle(color, ChatFormatting.BOLD))
            .append(Component.literal("! ★").withStyle(color, ChatFormatting.BOLD))

        world.server.playerList.broadcastSystemMessage(chatAnnouncement, false)
        SMPMod.messageChannel!!.sendMessage("**" + MarkdownSanitizer.escape(player.scoreboardName) + "** just got a **" + rarity.name + "** loot drop!").queue()
    }
}