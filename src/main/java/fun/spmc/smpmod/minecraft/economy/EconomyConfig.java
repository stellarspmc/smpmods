package fun.spmc.smpmod.minecraft.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public class EconomyConfig {

    private static final Map<Item, Double> CURRENCY_VALUES = new LinkedHashMap<>();

    static {
        CURRENCY_VALUES.put(Items.NETHER_STAR, 2500d);
        CURRENCY_VALUES.put(Items.NETHERITE_INGOT, 1000d);
        CURRENCY_VALUES.put(Items.DIAMOND, 100d);
        CURRENCY_VALUES.put(Items.GOLD_INGOT, 10d);
        CURRENCY_VALUES.put(Items.EMERALD, 5d);
        CURRENCY_VALUES.put(Items.LAPIS_LAZULI, 2d);
        CURRENCY_VALUES.put(Items.IRON_INGOT, 1d);
        CURRENCY_VALUES.put(Items.COPPER_INGOT, .1);
    }

    public static double getItemValue(Item item) {
        return CURRENCY_VALUES.getOrDefault(item, 0.0);
    }

    public static Map<Item, Double> getSortedCurrencyValues() {
        return CURRENCY_VALUES;
    }
}