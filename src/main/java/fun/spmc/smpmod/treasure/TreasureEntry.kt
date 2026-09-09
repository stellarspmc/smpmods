package fun.spmc.smpmod.treasure;

import fun.spmc.smpmod.misc.ItemRarity;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashSet;
import java.util.Set;

public class TreasureEntry {
    private final String id;
    private final Item item;
    private final int count;
    private final ItemRarity rarity;
    private final int minY;
    private final int maxY;
    private final ResourceKey<Level> requiredDimension;
    private final Set<ResourceKey<Biome>> allowedBiomes;

    private TreasureEntry(Builder builder) {
        this.id = builder.id;
        this.item = builder.item;
        this.count = builder.count;
        this.rarity = builder.rarity;
        this.minY = builder.minY;
        this.maxY = builder.maxY;
        this.requiredDimension = builder.requiredDimension;
        this.allowedBiomes = builder.allowedBiomes;
    }

    public boolean isValid(Level level, Holder<Biome> biome, int y, ItemRarity maxAllowedRarity) {
        if (this.rarity.ordinal() > maxAllowedRarity.ordinal()) return false;
        if (y < minY || y > maxY) return false;
        if (requiredDimension != null && level.dimension() != requiredDimension) return false;
        if (!allowedBiomes.isEmpty() && biome.unwrapKey().map(key -> !allowedBiomes.contains(key)).orElse(true)) return false;

        return true;
    }

    public ItemStack createStack() { return new ItemStack(item, count); }
    public ItemRarity getRarity() { return rarity; }

    public static class Builder {
        private final String id;
        private final Item item;
        private int count = 1;
        private ItemRarity rarity = ItemRarity.COMMON;
        private int minY = -64;
        private int maxY = 320;
        private ResourceKey<Level> requiredDimension = null;
        private final Set<ResourceKey<Biome>> allowedBiomes = new HashSet<>();

        public Builder(String id, Item item) {
            this.id = id;
            this.item = item;
        }

        public Builder count(int count) { this.count = count; return this; }
        public Builder rarity(ItemRarity rarity) { this.rarity = rarity; return this; }
        public Builder yRange(int minY, int maxY) { this.minY = minY; this.maxY = maxY; return this; }
        public Builder dimension(ResourceKey<Level> dimension) { this.requiredDimension = dimension; return this; }
        public Builder addBiome(ResourceKey<Biome> biome) { this.allowedBiomes.add(biome); return this; }
        public TreasureEntry build() { return new TreasureEntry(this); }
    }
}
