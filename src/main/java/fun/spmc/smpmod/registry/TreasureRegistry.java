package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.treasure.TreasureEntry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.ArrayList;
import java.util.List;

public class TreasureRegistry {
    private static final List<TreasureEntry> REGISTRY = new ArrayList<>();

    public static void register() {
        REGISTRY.clear();

        // Common Overworld Drops
        add("raw_iron_cluster", Items.RAW_IRON).count(3).rarity(ItemRarity.COMMON);
        add("raw_gold_cluster", Items.RAW_GOLD).count(2).rarity(ItemRarity.UNCOMMON).yRange(-64, 32);

        // Biome Specific (Badlands Gold / Terracotta)
        add("badlands_gold", Items.GOLD_BLOCK)
                .rarity(ItemRarity.RARE)
                .addBiome(Biomes.BADLANDS)
                .addBiome(Biomes.ERODED_BADLANDS);

        // Deepslate / Hard-Capped High Tiers (Mythic+)
        add("ancient_debris_chunk", Items.ANCIENT_DEBRIS)
                .rarity(ItemRarity.MYTHIC)
                .yRange(-64, -16);

        add("echo_core", Items.ECHO_SHARD)
                .count(2)
                .rarity(ItemRarity.LEGENDARY)
                .addBiome(Biomes.DEEP_DARK)
                .yRange(-64, -32);

        add("nether_star_fragment", Items.NETHER_STAR)
                .rarity(ItemRarity.CELESTIAL)
                .dimension(Level.NETHER);
    }

    public static List<TreasureEntry> getEligibleTreasures(Level level, Holder<Biome> biome, int y, ItemRarity maxCap) { return REGISTRY.stream().filter(entry -> entry.isValid(level, biome, y, maxCap)).toList(); }
    private static TreasureEntry.Builder add(String id, Item item) {
        TreasureEntry.Builder builder = new TreasureEntry.Builder(id, item);
        REGISTRY.add(builder.build());
        return builder;
    }

}
