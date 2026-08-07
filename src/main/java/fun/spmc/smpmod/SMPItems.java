package fun.spmc.smpmod;

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

    public static void register() {
        create("normal_fishing_rod", properties -> new RodItem(properties, RodTiers.NORMAL));
        create("copper_fishing_rod", properties -> new RodItem(properties, RodTiers.COPPER));
        create("iron_fishing_rod", properties -> new RodItem(properties, RodTiers.IRON));
        create("gold_fishing_rod", properties -> new RodItem(properties, RodTiers.GOLD));
        create("emerald_fishing_rod", properties -> new RodItem(properties, RodTiers.EMERALD));
        create("diamond_fishing_rod", properties -> new RodItem(properties, RodTiers.DIAMOND));
        create("netherite_fishing_rod", properties -> new RodItem(properties, RodTiers.NETHERITE));

        FISH.add(create("cod", properties -> new FishItem(properties, Items.COD, "Cod", 1.5)));
        FISH.add(create("salmon", properties -> new FishItem(properties, Items.SALMON, "Salmon", 2)));
    }
}
