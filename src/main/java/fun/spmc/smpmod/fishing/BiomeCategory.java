package fun.spmc.smpmod.fishing;

import fun.spmc.smpmod.fishing.fish.FishItem;
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

import java.util.ArrayList;
import java.util.List;

public enum BiomeCategory {
    LAVA(PolymerFishes.LAVA),
    VOID(PolymerFishes.END),
    DEEP(PolymerFishes.DEEP),
    SNOWY(PolymerFishes.SNOWY),
    DESERT(PolymerFishes.DESERT),
    TROPICAL(PolymerFishes.TROPICAL),
    PLAINS(PolymerFishes.PLAINS),
    DEFAULT(new ArrayList<>());

    private final List<Item> fishArray;

    BiomeCategory(List<Item> fishArray) { this.fishArray = fishArray; }
    public List<Item> getFishArray() { return fishArray; }

    private static final TagKey<Biome> C_DESERT = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_desert"));
    private static final TagKey<Biome> C_SNOWY = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_snowy"));
    private static final TagKey<Biome> C_JUNGLE = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_jungle"));
    private static final TagKey<Biome> C_OCEAN = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_ocean"));

    public static BiomeCategory getCategory(ServerPlayer player) {
        Holder<Biome> biomeHolder = player.level().getBiome(player.getOnPos());
        boolean isInLava = player.isInLava();
        double yPos = player.getY();
        boolean isEndDimension = player.level().dimension() == ServerLevel.END;
        if (isInLava) return LAVA;
        if (isEndDimension && yPos < 0) return VOID;
        if (yPos < 0) return DEEP;

        if (biomeHolder.is(C_SNOWY)) return SNOWY;
        if (biomeHolder.is(C_DESERT) || biomeHolder.is(BiomeTags.IS_BADLANDS)) return DESERT;
        if (biomeHolder.is(C_JUNGLE) || biomeHolder.is(BiomeTags.IS_SAVANNA)) return TROPICAL;
        if (biomeHolder.is(BiomeTags.IS_HILL) || biomeHolder.is(C_OCEAN)) return PLAINS;

        return DEFAULT;
    }
}