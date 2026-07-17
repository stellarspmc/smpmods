package fun.spmc.smpmod.minecraft.treasure;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

import static fun.spmc.smpmod.SMPMod.messageChannel;

public class TreasureSpawner {
    public static void spawnTreasureContainer(ServerLevel world, BlockPos pos, String rarity, ResourceKey<LootTable> lootTable) {
        world.destroyBlock(pos, true);
        world.setBlock(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 3);

        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
            barrel.setLootTable(lootTable, world.getRandom().nextLong());

            String cleanName = rarity.substring(0, 1).toUpperCase() + rarity.substring(1) + " Treasure";
            Component customNameComponent = Component.literal(cleanName)
                    .withStyle(style -> style.withColor(0xFFFFFF).withBold(false).withItalic(false));

            barrel.applyComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, customNameComponent)
                    .build(), DataComponentPatch.builder()
                    .set(DataComponents.CUSTOM_NAME, customNameComponent)
                    .build());

            barrel.setChanged();
        }

        spawnLootEffects(world, pos, rarity);
    }

    public static void spawnLootEffects(ServerLevel world, BlockPos pos, String rarity) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        switch (rarity) {

            case "common":
                world.sendParticles(ParticleTypes.CRIT, x, y, z, 20, 0.3, 0.3, 0.3, 0.1);
                world.sendParticles(ParticleTypes.SMOKE, x, y, z, 10, 0.2, 0.2, 0.2, 0.02);
                world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
                break;

            case "rare":
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 35, 0.4, 0.4, 0.4, 0.05);
                world.sendParticles(ParticleTypes.GLOW, x, y, z, 20, 0.3, 0.3, 0.3, 0.02);
                world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                break;

            case "epic":
                world.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1), x, y, z, 60, 0.5, 0.5, 0.5, 0.03);
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 25, 0.4, 0.4, 0.4, 0.08);
                world.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.7f, 1.3f);
                break;

            case "legendary":
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 120, 0.6, 0.6, 0.6, 0.3);
                world.sendParticles(ParticleTypes.FIREWORK, x, y, z, 40, 0.4, 0.4, 0.4, 0.15);
                world.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0f, 1.0f);

                announceLoot(world, pos, "Legendary", ChatFormatting.GOLD);
                break;

            case "mythical":
                world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF55FF), x, y, z, 2, 0, 0, 0, 0);
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 200, 0.8, 0.8, 0.8, 0.5);
                world.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 80, 0.5, 0.5, 0.5, 0.2);
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 60, 0.5, 0.5, 0.5, 0.1);

                world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.7f, 1.5f);
                world.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0f, 0.9f);

                announceLoot(world, pos, "Mythical", ChatFormatting.LIGHT_PURPLE);
                break;
        }
    }

    private static void announceLoot(ServerLevel world, BlockPos pos, String rarityName, ChatFormatting color) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        ServerPlayer player = (ServerPlayer) world.getNearestPlayer(x, y, z, 10.0, false);
        String playerName = (player != null) ? player.getScoreboardName() : "Someone";

        Component chatAnnouncement = Component.literal("★ ")
                .withStyle(color, ChatFormatting.BOLD)
                .append(Component.literal(playerName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .append(Component.literal(" found a ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(rarityName + " Drop").withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal("! ★").withStyle(color, ChatFormatting.BOLD));

        world.getServer().getPlayerList().broadcastSystemMessage(chatAnnouncement, false);

        messageChannel.sendMessage("**" + playerName + "** just got a **" + rarityName + "** loot drop!").queue();
    }
}