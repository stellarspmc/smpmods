package fun.spmc.smpmod.fishing;

import fun.spmc.smpmod.registry.PolymerFishes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

public enum BiomeCategory {
    LAVA(PolymerFishes.LAVA),
    VOID(PolymerFishes.END),
    DEEP(PolymerFishes.DEEP),
    SNOWY(PolymerFishes.SNOWY),
    DESERT(PolymerFishes.DESERT),
    TROPICAL(PolymerFishes.TROPICAL),
    PLAINS(PolymerFishes.PLAINS),
    SKY(PolymerFishes.SKY),
    DEFAULT(PolymerFishes.FISH);

    private final List<Item> fishArray;
    private static final Map<Item, BiomeCategory> FISH_TO_CATEGORY = new IdentityHashMap<>();

    BiomeCategory(List<Item> fishArray) { this.fishArray = fishArray; }
    public List<Item> getFishArray() { return fishArray; }

    private static final TagKey<Biome> C_COLD = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_cold"));
    private static final TagKey<Biome> C_SNOWY = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_snowy"));

    private static final TagKey<Biome> C_DESERT = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_desert"));
    private static final TagKey<Biome> C_DRY = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_dry"));

    private static final TagKey<Biome> C_JUNGLE = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_jungle"));
    private static final TagKey<Biome> C_SAVANNA = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_savanna"));
    private static final TagKey<Biome> C_SWAMP = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_swamp"));
    private static final TagKey<Biome> C_TROPICAL = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_tropical"));

    private static final TagKey<Biome> C_OCEAN = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_ocean"));
    private static final TagKey<Biome> C_RIVER = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_river"));

    public static List<Item> getAvailableFish(ServerPlayer player) {
        Holder<Biome> biomeHolder = player.level().getBiome(player.getOnPos());
        double yPos = player.getY();

        Set<BiomeCategory> matchingCategories = EnumSet.noneOf(BiomeCategory.class);

        if (biomeHolder.is(C_SNOWY) || biomeHolder.is(C_COLD)) matchingCategories.add(SNOWY);
        if (biomeHolder.is(C_DESERT) || biomeHolder.is(C_DRY) || biomeHolder.is(BiomeTags.IS_BADLANDS)) matchingCategories.add(DESERT);
        if (biomeHolder.is(C_JUNGLE) || biomeHolder.is(C_SAVANNA) || biomeHolder.is(C_SWAMP) || biomeHolder.is(C_TROPICAL) || biomeHolder.is(BiomeTags.IS_SAVANNA) || biomeHolder.is(BiomeTags.IS_JUNGLE)) matchingCategories.add(TROPICAL);
        if (biomeHolder.is(C_OCEAN) || biomeHolder.is(C_RIVER) || biomeHolder.is(BiomeTags.IS_OCEAN) || biomeHolder.is(BiomeTags.IS_RIVER) || biomeHolder.is(BiomeTags.IS_HILL) || biomeHolder.is(BiomeTags.IS_TAIGA) || biomeHolder.is(BiomeTags.IS_BEACH)) matchingCategories.add(PLAINS);

        if (yPos > 240) {
            matchingCategories.clear();
            matchingCategories.add(SKY);
        }
        if (yPos < 0) {
            matchingCategories.clear();
            matchingCategories.add(DEEP);
        }

        if (player.level().dimension() == ServerLevel.NETHER) {
            matchingCategories.clear();
            matchingCategories.add(LAVA);
        }
        if (player.level().dimension() == ServerLevel.END) {
            matchingCategories.clear();
            matchingCategories.add(VOID);
        }

        List<Item> combinedFish = new ArrayList<>();
        for (BiomeCategory category : matchingCategories) combinedFish.addAll(category.getFishArray());
        combinedFish.addAll(DEFAULT.getFishArray());
        return combinedFish;
    }

    public static void initLookupMap() { // init
        FISH_TO_CATEGORY.clear();
        for (BiomeCategory category : values()) {
            if (category == DEFAULT) continue;
            for (Item fish : category.getFishArray()) FISH_TO_CATEGORY.putIfAbsent(fish, category);
        }
    }

    public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }

    public static BiomeCategory getBiomeFish(Item fish) {
        return FISH_TO_CATEGORY.getOrDefault(fish, DEFAULT);
    }
}