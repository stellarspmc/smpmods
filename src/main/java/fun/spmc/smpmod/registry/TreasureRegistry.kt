package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.core.ItemRarity;
import fun.spmc.smpmod.treasure.TreasureEntry;
import fun.spmc.smpmod.treasure.TreasureHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class TreasureRegistry {
    private static final List<TreasureEntry> REGISTRY = new ArrayList<>();

    public static void register() {
        REGISTRY.clear();

        add("raw_iron_cluster", Items.RAW_IRON).count(3).rarity(ItemRarity.COMMON);
        add("raw_gold_cluster", Items.RAW_GOLD).count(2).rarity(ItemRarity.UNCOMMON);
        add("badlands_gold", Items.GOLD_BLOCK).rarity(ItemRarity.RARE).biome(TreasureHelper.Biomes.BADLANDS);
        add("ancient_debris_chunk", Items.ANCIENT_DEBRIS).rarity(ItemRarity.MYTHIC);
        add("echo_core", Items.ECHO_SHARD).count(2).rarity(ItemRarity.LEGENDARY).biome(TreasureHelper.Biomes.SCULK);
        add("nether_star_fragment", Items.NETHER_STAR).rarity(ItemRarity.ASTRAL).dimension(Level.NETHER);
    }

    public static List<TreasureEntry> getEligibleTreasures(Level level, TreasureHelper.Biomes biome, ItemRarity rarity) { return REGISTRY.stream().filter(entry -> entry.isValid(level, rarity, biome)).toList(); }
    private static TreasureEntry.Builder add(String id, Item item) {
        TreasureEntry.Builder builder = new TreasureEntry.Builder(id, item);
        REGISTRY.add(builder.build());
        return builder;
    }
}
