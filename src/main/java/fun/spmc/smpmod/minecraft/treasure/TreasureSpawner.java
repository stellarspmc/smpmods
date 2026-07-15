package fun.spmc.smpmod.minecraft.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

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

        // TODO: add particles
        world.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.0);
        world.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.AMBIENT, 1.0f, 1.0f);
    }
}