package fun.spmc.smpmod.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TreasureEvents {
    public static double eventPercentage = 1f;

    public static void onBlockBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity ignoredBlockEntity) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack mainHand = player.getMainHandItem();
        var enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var silkTouchHolder = enchantmentRegistry.get(Enchantments.SILK_TOUCH);
        if (silkTouchHolder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), mainHand) > 0) return;

        ResourceKey<Biome> biomeKey = world.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getResourceKey(world.getBiome(pos).value())
                .orElse(Biomes.PLAINS);

        String folderName = getFolderFromBiome(biomeKey);

        double fatigueMultiplier = TreasureFatigue.getMultiplier(player.getUUID()) * eventPercentage;

        if (EnchantmentHelper.getItemEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.EFFICIENCY), mainHand) > 5) fatigueMultiplier *= Math.pow(0.65, EnchantmentHelper.getItemEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.EFFICIENCY), mainHand) - 5);
        if (player.getEffect(MobEffects.HASTE) != null && Objects.requireNonNull(player.getEffect(MobEffects.HASTE)).getAmplifier() > 1) fatigueMultiplier *= Math.pow(0.55, Objects.requireNonNull(player.getEffect(MobEffects.HASTE)).getAmplifier() - 1);

        Rarity rarity = rollTreasureRarity(state, fatigueMultiplier, world.getRandom(), world.dimension());
        if (rarity == null) return;

        String rarityName = rarity.getName();

        TreasureFatigue.recordTreasure(player.getUUID());

        Identifier tableLocation = rarityName.equals("mythical")
                ? Identifier.fromNamespaceAndPath("treasure", "mythical/mythical")
                : Identifier.fromNamespaceAndPath("treasure", folderName + "/" + rarityName);

        ResourceKey<LootTable> lootTableUri = ResourceKey.create(Registries.LOOT_TABLE, tableLocation);

        TreasureSpawner.spawnTreasureContainer((ServerLevel) world, pos, rarityName, lootTableUri, serverPlayer);
    }

    private static final float THRESHOLD_MYTHICAL = .008f;
    private static final float THRESHOLD_LEGENDARY = THRESHOLD_MYTHICAL + .016f;
    private static final float THRESHOLD_EPIC = THRESHOLD_LEGENDARY + .032f;
    private static final float THRESHOLD_RARE = THRESHOLD_EPIC + .25f;
    private static final float THRESHOLD_COMMON = THRESHOLD_RARE + 1;

    private static final Set<Block> OVERWORLD_HIGH_ORES = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE
    );

    private static final Set<Block> OVERWORLD_MID_DEEPSLATE_ORES = Set.of(
            Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE
    );

    private static final Set<Block> OVERWORLD_MID_STONE_ORES = Set.of(
            Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE
    );

    private static final Set<Block> OVERWORLD_STONES = Set.of(
            Blocks.STONE, Blocks.TUFF, Blocks.ANDESITE, Blocks.GRANITE,
            Blocks.AMETHYST_BLOCK, Blocks.DRIPSTONE_BLOCK, Blocks.DIORITE, Blocks.DEEPSLATE
    );

    private static final Set<Block> OVERWORLD_LOW = Set.of(Blocks.CALCITE, Blocks.SANDSTONE);

    private static final Set<Block> NETHER_ORES = Set.of(Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE);
    private static final Set<Block> NETHER_STONES = Set.of(Blocks.BASALT, Blocks.BLACKSTONE, Blocks.SMOOTH_BASALT, Blocks.MAGMA_BLOCK);
    private static final Set<Block> NETHER_LOW = Set.of(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);

    private static final Map<ResourceKey<Biome>, String> BIOME_FOLDER_CACHE = new ConcurrentHashMap<>();

    public static boolean rigTreasures = false;

    public static Rarity rollTreasureRarity(BlockState state, double fatigueMultiplier, RandomSource random, ResourceKey<Level> dimension) {
        float commonChance = (float) (getBaseCommonChance(state, dimension) * fatigueMultiplier);
        if (commonChance <= 0) return null;
        float val = (random.nextFloat() * 100f) / commonChance;
        if (val < THRESHOLD_MYTHICAL) return Rarity.MYTHICAL;
        if (val < THRESHOLD_LEGENDARY) return adjustRarity(Rarity.LEGENDARY);
        if (val < THRESHOLD_EPIC) return adjustRarity(Rarity.EPIC);
        if (val < THRESHOLD_RARE) return adjustRarity(Rarity.RARE);
        if (val < THRESHOLD_COMMON) return adjustRarity(Rarity.COMMON);
        return null;
    }

    private static Rarity adjustRarity(Rarity rarity) {
        if (!rigTreasures) return rarity;
        return switch (rarity) {
            case LEGENDARY -> Rarity.MYTHICAL;
            case EPIC -> Rarity.LEGENDARY;
            case RARE -> Rarity.EPIC;
            case COMMON -> Rarity.RARE;
            default -> rarity;
        };
    }

    private static float getBaseCommonChance(BlockState state, ResourceKey<Level> dimension) {
        Block block = state.getBlock();

        if (dimension == Level.OVERWORLD) {
            if (OVERWORLD_HIGH_ORES.contains(block)) return 2.5f;
            if (OVERWORLD_MID_DEEPSLATE_ORES.contains(block)) return 1.75f;
            if (OVERWORLD_MID_STONE_ORES.contains(block)) return 1.5f;
            if (OVERWORLD_STONES.contains(block) || state.is(BlockTags.TERRACOTTA)) return .95f;
            if (OVERWORLD_LOW.contains(block)) return .5f;
        } else if (dimension == Level.NETHER) {
            if (NETHER_ORES.contains(block)) return 1.5f;
            if (NETHER_STONES.contains(block)) return .96f;
            if (NETHER_LOW.contains(block)) return .01f;
        } else if (dimension == Level.END) return 0.55f;
        return 0;
    }

    public static String getFolderFromBiome(ResourceKey<Biome> biomeKey) {
        return BIOME_FOLDER_CACHE.computeIfAbsent(biomeKey, key -> {
            String path = key.identifier().getPath();

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

            if (key.identifier().getNamespace().equals("minecraft") && path.contains("end")) return "end";

            return "default";
        });
    }

    public enum Rarity {
        MYTHICAL("mythical"),
        LEGENDARY("legendary"),
        EPIC("epic"),
        RARE("rare"),
        COMMON("common");

        private final String name;
        Rarity(String name) { this.name = name; }
        public String getName() { return name; }
    }
}
