package fun.spmc.smpmod.minecraft.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public class EconomyConfig {

    private static final Map<Item, Double> CURRENCY_VALUES = new LinkedHashMap<>();

    static {
        CURRENCY_VALUES.put(Items.NETHER_STAR, 2500.0);
        CURRENCY_VALUES.put(Items.NETHERITE_INGOT, 1000.0);
        CURRENCY_VALUES.put(Items.DIAMOND, 100.0);
        CURRENCY_VALUES.put(Items.EMERALD, 15.0);
        CURRENCY_VALUES.put(Items.GOLD_INGOT, 10.0);
        CURRENCY_VALUES.put(Items.REDSTONE, 2.5);
        CURRENCY_VALUES.put(Items.LAPIS_LAZULI, 2.0);
        CURRENCY_VALUES.put(Items.IRON_INGOT, 1.0);
        CURRENCY_VALUES.put(Items.COPPER_INGOT, 0.1);
    }

    public static double getItemValue(Item item) {
        return CURRENCY_VALUES.getOrDefault(item, 0.0);
    }

    public static Map<Item, Double> getSortedCurrencyValues() {
        return CURRENCY_VALUES;
    }
}