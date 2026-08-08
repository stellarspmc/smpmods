package fun.spmc.smpmod;

import fun.spmc.smpmod.fishing.FishRarity;
import fun.spmc.smpmod.fishing.fish.FishItem;
import fun.spmc.smpmod.fishing.rod.RodItem;
import fun.spmc.smpmod.fishing.rod.RodTiers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SMPItems {
    public static List<Item> FISH = new ArrayList<>();
    private static <T extends Item> T create(String id, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smpmod", id));
        Item.Properties properties = new Item.Properties().setId(key);
        T item = factory.apply(properties);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static void registerRod(String id, RodTiers tier) {
        create(id, properties -> new RodItem(properties, tier));
    }

    private static void registerFish(String id, Item vanillaModel, double basePrice, FishRarity rarity) {
        Item item = create(id, properties -> new FishItem(properties, vanillaModel, formatName(id), basePrice, rarity));
        FISH.add(item);
    }

    private static String formatName(String id) {
        String[] words = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static void register() {
        registerRod("normal_fishing_rod", RodTiers.NORMAL);
        registerRod("copper_fishing_rod", RodTiers.COPPER);
        registerRod("iron_fishing_rod", RodTiers.IRON);
        registerRod("gold_fishing_rod", RodTiers.GOLD);
        registerRod("emerald_fishing_rod", RodTiers.EMERALD);
        registerRod("diamond_fishing_rod", RodTiers.DIAMOND);
        registerRod("netherite_fishing_rod", RodTiers.NETHERITE);

        registerFish("cod", Items.COD, 1.5, FishRarity.COMMON);
        registerFish("salmon", Items.SALMON, 2, FishRarity.COMMON);
        registerFish("tropical_fish", Items.TROPICAL_FISH, 3, FishRarity.COMMON);
        registerFish("red_bass", Items.SALMON, 3.5, FishRarity.COMMON);
    }
}
