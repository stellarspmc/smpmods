package fun.spmc.smpmod.minecraft.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public class TreasureEvents {
    private static final float COMMON_PERCENTAGE = 65;
    private static final float RARE_PERCENTAGE = 25 + COMMON_PERCENTAGE;
    private static final float EPIC_PERCENTAGE = 7.5f + RARE_PERCENTAGE;

    public static void onBlockBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity ignoredBlockEntity) {
        ResourceKey<Level> dimension = world.dimension();
        boolean isValidBlock = false;

        if (player.getRandom().nextFloat() > .05f) return;
        if (dimension == Level.OVERWORLD) isValidBlock = state.is(BlockTags.STONE_ORE_REPLACEABLES)
                    || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                    || state.is(Blocks.DEEPSLATE_COAL_ORE) || state.is(Blocks.COAL_ORE)
                    || state.is(BlockTags.COPPER_ORES)
                    || state.is(BlockTags.IRON_ORES)
                    || state.is(BlockTags.GOLD_ORES)
                    || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) || state.is(Blocks.DIAMOND_ORE)
                    || state.is(Blocks.DEEPSLATE_EMERALD_ORE) || state.is(Blocks.EMERALD_ORE)
                    || state.is(Blocks.DEEPSLATE_REDSTONE_ORE) || state.is(Blocks.REDSTONE_ORE)
                    || state.is(Blocks.DEEPSLATE_LAPIS_ORE) || state.is(Blocks.LAPIS_ORE);
        else if (dimension == Level.NETHER) isValidBlock = state.is(Blocks.NETHERRACK)
                    || state.is(Blocks.NETHER_QUARTZ_ORE)
                    || state.is(Blocks.NETHER_GOLD_ORE)
                    || state.is(Blocks.ANCIENT_DEBRIS)
                    || state.is(Blocks.BLACKSTONE)
                    || state.is(Blocks.BASALT)
                    || state.is(Blocks.SMOOTH_BASALT)
                    || state.is(Blocks.GILDED_BLACKSTONE)
                    || state.is(BlockTags.NYLIUM);
        else if (dimension == Level.END) isValidBlock = state.is(Blocks.END_STONE);

        if (!isValidBlock) return;

        ResourceKey<Biome> biomeKey = world.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getResourceKey(world.getBiome(pos).value())
                .orElse(Biomes.PLAINS);

        String folderName = getFolderFromBiome(biomeKey);

        String rarity = "legendary";
        float random = player.getRandom().nextFloat();
        if (random < COMMON_PERCENTAGE * .01f) {
            rarity = "common";
        } else if (random < RARE_PERCENTAGE * .01f) {
            rarity = "rare";
        } else if (random < EPIC_PERCENTAGE * .01f) {
            rarity = "epic";
        }

        Identifier tableLocation = Identifier.fromNamespaceAndPath("treasure", folderName + "/" + rarity);
        ResourceKey<LootTable> lootTableUri = ResourceKey.create(Registries.LOOT_TABLE, tableLocation);

        TreasureSpawner.spawnTreasureContainer((ServerLevel) world, pos, rarity, lootTableUri);
    }

    private static String getFolderFromBiome(ResourceKey<Biome> biomeKey) {
        Identifier id = biomeKey.identifier();
        String path = id.getPath();

        if (path.contains("badlands")) return "badlands";
        if (path.contains("desert")) return "desert";
        if (path.contains("dripstone")) return "dripstone";
        if (path.contains("dark_forest")) return "dark_forest";
        if (path.contains("deep_dark")) return "deep_dark";
        if (path.contains("lush_caves")) return "lush_caves";
        if (path.contains("mushroom")) return "mushroom";
        if (path.contains("swamp")) return "swamp";
        if (path.contains("jungle")) return "jungle";
        if (path.contains("taiga")) return "taiga";
        if (path.contains("savanna")) return "savanna";
        if (path.contains("ocean") || path.contains("beach")) return "ocean";

        if (path.contains("flower") || path.contains("meadow") || path.contains("cherry")) return "flower";
        if (path.contains("ice") || path.contains("frozen") || path.contains("snow")) return "ice";
        if (path.contains("peaks") || path.contains("slopes") || path.contains("stony")) return "mountain";
        if (path.contains("windswept")) return "wind";

        if (path.contains("basalt_deltas")) return "basalt";
        if (path.contains("crimson_forest")) return "crimson";
        if (path.contains("warped_forest")) return "warped";
        if (path.contains("soul_sand_valley")) return "soul_valley";
        if (path.contains("nether_wastes")) return "nether";

        if (id.getNamespace().equals("minecraft") && path.contains("end")) return "end";

        return "default";
    }
}
