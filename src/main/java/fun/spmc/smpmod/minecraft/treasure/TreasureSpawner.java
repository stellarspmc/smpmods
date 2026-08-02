package fun.spmc.smpmod.minecraft.treasure;

import fun.spmc.smpmod.discord.MarkdownParser;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

import static fun.spmc.smpmod.SMPMod.messageChannel;

public class TreasureSpawner {
    public static void spawnTreasureContainer(ServerLevel world, BlockPos pos, String rarity, ResourceKey<LootTable> lootTable, ServerPlayer player) {
        world.destroyBlock(pos, true);
        world.setBlock(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 3);

        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
            barrel.setLootTable(lootTable, world.getRandom().nextLong());
            barrel.setChanged();
        }

        spawnLootEffects(world, pos, rarity, player);
    }

    public static void spawnLootEffects(ServerLevel world, BlockPos pos, String rarity, ServerPlayer player) {
        double x = pos.getX() + .5;
        double y = pos.getY() + .5;
        double z = pos.getZ() + .5;

        switch (rarity) {
            case "common" -> {
                world.sendParticles(ParticleTypes.CRIT, x, y, z, 20, .3, .3, .3, .1);
                world.sendParticles(ParticleTypes.SMOKE, x, y, z, 10, .2, .2, .2, .02);
                world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, .8f, 1.2f);
            } case "rare" -> {
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 35, .4, .4, .4, .05);
                world.sendParticles(ParticleTypes.GLOW, x, y, z, 20, .3, .3, .3, .02);
                world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1, 1);
                world.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, .5f, 1.5f);
            } case "epic" -> {
                world.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1), x, y, z, 60, .5, .5, .5, .03);
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 25, .4, .4, .4, .08);
                world.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1, 1);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, .7f, 1.3f);
            } case "legendary" -> {
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 120, .6, .6, .6, .3);
                world.sendParticles(ParticleTypes.FIREWORK, x, y, z, 40, .4, .4, .4, .15);
                world.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1, 1);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1, 1);

                announceLoot(world, "Legendary", ChatFormatting.GOLD, player);
            } case "mythical" -> {
                world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF55FF), x, y, z, 2, 0, 0, 0, 0);
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 200, .8, .8, .8, .5);
                world.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 80, .5, .5, .5, .2);
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 60, .5, .5, .5, .1);

                world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, .7f, 1.5f);
                world.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1, .8f);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1, .9f);

                announceLoot(world, "Mythical", ChatFormatting.LIGHT_PURPLE, player);
            }
        }
    }

    private static void announceLoot(ServerLevel world, String rarityName, ChatFormatting color, ServerPlayer player) {
        EconomySavedData eco = EconomySavedData.get(world);
        double balance = eco.getBalance(player.getUUID());
        double balanceScale = (balance <= 0) ? 1 : Math.clamp(1000 / balance, 0, 1);
        eco.changeBalance(player.getUUID(), 3 * balanceScale);

        Component chatAnnouncement = Component.literal("★ ")
                .withStyle(color, ChatFormatting.BOLD)
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .append(Component.literal(" found a ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(rarityName + " Drop").withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal("! ★").withStyle(color, ChatFormatting.BOLD));

        world.getServer().getPlayerList().broadcastSystemMessage(chatAnnouncement, false);
        messageChannel.sendMessage("**" + MarkdownParser.escapeMarkdown(player.getScoreboardName()) + "** just got a **" + rarityName + "** loot drop!").queue();
    }
}