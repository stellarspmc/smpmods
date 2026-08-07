package fun.spmc.smpmod;

import fun.spmc.smpmod.fishing.fish.FishItem;
import fun.spmc.smpmod.fishing.rod.RodItem;
import fun.spmc.smpmod.fishing.rod.RodTiers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class SMPItems {
    public static List<Item> FISH;

    private static <T extends Item> Item register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("smpmod", name), item);
    }

    public static void register() {
        register("normal_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.NORMAL));
        register("copper_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.COPPER));
        register("iron_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.IRON));
        register("gold_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.GOLD));
        register("emerald_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.EMERALD));
        register("diamond_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.DIAMOND));
        register("netherite_fishing_rod", new RodItem(new Item.Properties().stacksTo(1), RodTiers.NETHERITE));

        FISH.add(register("cod", new FishItem(new Item.Properties(), Items.COD, "Cod", 1.5)));
        FISH.add(register("salmon", new FishItem(new Item.Properties(), Items.SALMON, "Salmon", 2)));
    }
}
