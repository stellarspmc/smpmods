package fun.spmc.smpmod.minecraft.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public class TreasureEvents {
    public static void onBlockBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity ignoredBlockEntity) {
        ResourceKey<Biome> biomeKey = world.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getResourceKey(world.getBiome(pos).value())
                .orElse(Biomes.PLAINS);

        String folderName = getFolderFromBiome(biomeKey);

        String rarity = rollTreasureRarity(state, player.getRandom(), world.dimension());
        if (rarity == null) return;

        Identifier tableLocation = rarity.equals("mythical") ? Identifier.fromNamespaceAndPath("treasure", "mythical/mythical") : Identifier.fromNamespaceAndPath("treasure", folderName + "/" + rarity);
        ResourceKey<LootTable> lootTableUri = ResourceKey.create(Registries.LOOT_TABLE, tableLocation);

        TreasureSpawner.spawnTreasureContainer((ServerLevel) world, pos, rarity, lootTableUri);
    }

    private static String rollTreasureRarity(BlockState state, RandomSource random, ResourceKey<Level> dimension) {
        float commonChance = getBaseCommonChance(state, dimension);
        if (commonChance <= 0.0f) return null;

        float roll = random.nextFloat() * 100f;

        float mythicalThreshold = commonChance * .008f;
        float legendaryThreshold = mythicalThreshold + (commonChance * .016f);
        float epicThreshold = legendaryThreshold + (commonChance * .032f);
        float rareThreshold = epicThreshold + (commonChance * .25f);
        float commonThreshold = rareThreshold + commonChance;

        if (roll < mythicalThreshold) return "mythical";
        else if (roll < legendaryThreshold) return "legendary";
        else if (roll < epicThreshold) return "epic";
        else if (roll < rareThreshold) return "rare";
        else if (roll < commonThreshold) return "common";
        return null;
    }

    private static float getBaseCommonChance(BlockState state, ResourceKey<Level> dimension) {
        if (dimension == Level.OVERWORLD) {
            if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) ||
                    state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)) return 2.5f;
            if (state.is(Blocks.DEEPSLATE_COAL_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE) ||
                    state.is(Blocks.DEEPSLATE_COPPER_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE) ||
                    state.is(Blocks.DEEPSLATE_REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)) return 1.75f;
            if (state.is(Blocks.COAL_ORE) || state.is(Blocks.IRON_ORE) ||
                    state.is(Blocks.COPPER_ORE) || state.is(Blocks.GOLD_ORE) ||
                    state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.LAPIS_ORE)) return 1.5f;
            if (state.is(Blocks.STONE) || state.is(Blocks.TUFF) || state.is(Blocks.ANDESITE) || state.is(Blocks.GRANITE) ||
                    state.is(Blocks.AMETHYST_BLOCK) || state.is(Blocks.DRIPSTONE_BLOCK) ||
                    state.is(BlockTags.TERRACOTTA) || state.is(Blocks.DIORITE) || state.is(Blocks.DEEPSLATE)) return .95f;
            if (state.is(Blocks.CALCITE) || state.is(Blocks.SANDSTONE)) return .5f;
        }

        if (dimension == Level.NETHER) {
            if (state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE)) return 1.5f;
            if (state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.MAGMA_BLOCK)) return .96f;
            if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) return .01f;
        }

        if (dimension == Level.END || state.is(Blocks.END_STONE)) return 1f;

        return 0f; // Not a valid block
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
