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

@SuppressWarnings("ALL")
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

        registerFish("cod", Items.COD, 15, FishRarity.COMMON);
        registerFish("seafish", Items.COD, 21, FishRarity.COMMON);
        registerFish("joes", Items.COD, 23, FishRarity.COMMON);
        registerFish("salmon", Items.SALMON, 23, FishRarity.COMMON);
        registerFish("valey", Items.SALMON, 24, FishRarity.COMMON);
        registerFish("lilorange", Items.TROPICAL_FISH, 26, FishRarity.COMMON);
        registerFish("coalbie", Items.SALMON, 27, FishRarity.COMMON);
        registerFish("gegg", Items.PUFFERFISH, 29, FishRarity.COMMON);
        registerFish("tropical_fish", Items.TROPICAL_FISH, 31, FishRarity.COMMON);
        registerFish("tikira", Items.COD, 32, FishRarity.COMMON);
        registerFish("red_bass", Items.SALMON, 33, FishRarity.COMMON);
        registerFish("zombish", Items.SALMON, 34, FishRarity.COMMON);
        registerFish("posie", Items.COD, 34, FishRarity.COMMON);
        registerFish("mavide", Items.PUFFERFISH, 37, FishRarity.COMMON);
        registerFish("porets", Items.SALMON, 38, FishRarity.COMMON);
        registerFish("clownfish", Items.TROPICAL_FISH, 39, FishRarity.COMMON);
        registerFish("pufferfish", Items.PUFFERFISH, 44, FishRarity.COMMON);
        registerFish("tuffie", Items.PUFFERFISH, 46, FishRarity.COMMON);
        registerFish("yoda", Items.SALMON, 48, FishRarity.COMMON);
        registerFish("quelk", Items.PUFFERFISH, 48, FishRarity.COMMON);
        registerFish("laugho", Items.TROPICAL_FISH, 49, FishRarity.COMMON);
        registerFish("folk", Items.TROPICAL_FISH, 53, FishRarity.COMMON);
        registerFish("plankton", Items.PUFFERFISH, 56, FishRarity.COMMON);
        registerFish("chicken_jockey", Items.SALMON, 62, FishRarity.COMMON);
        registerFish("cooper", Items.PUFFERFISH, 71, FishRarity.COMMON);
        registerFish("sheldon", Items.COD, 83, FishRarity.COMMON);
        registerFish("aure", Items.PUFFERFISH, 87, FishRarity.COMMON);

        registerFish("rowlen", Items.COD, 97, FishRarity.UNCOMMON);
        registerFish("skibindidle", Items.COD, 102, FishRarity.UNCOMMON);
        registerFish("crazer", Items.SALMON, 117, FishRarity.UNCOMMON);
        registerFish("venomfish", Items.PUFFERFISH, 183, FishRarity.UNCOMMON);
        registerFish("enemyfish", Items.TROPICAL_FISH, 198, FishRarity.UNCOMMON);
        registerFish("geysaurous", Items.SALMON, 237, FishRarity.UNCOMMON);
        registerFish("jenfish", Items.COD, 316, FishRarity.UNCOMMON);
        registerFish("tonka", Items.COD, 333, FishRarity.UNCOMMON);
        registerFish("deitus", Items.PUFFERFISH, 362, FishRarity.UNCOMMON);
        registerFish("mochafish", Items.TROPICAL_FISH, 414, FishRarity.UNCOMMON);
        registerFish("diddus", Items.SALMON, 428, FishRarity.UNCOMMON);
        registerFish("codded_salmon", Items.COD, 471, FishRarity.UNCOMMON);
        registerFish("kungdus_khan", Items.COD, 513, FishRarity.UNCOMMON);
        registerFish("swordfish", Items.TROPICAL_FISH, 592, FishRarity.UNCOMMON);
        registerFish("finfishius", Items.COD, 611, FishRarity.UNCOMMON);
        registerFish("phantomfish", Items.PUFFERFISH, 632, FishRarity.UNCOMMON);

        registerFish("glandsouris", Items.SALMON, 527, FishRarity.RARE);
        registerFish("jumpus", Items.PUFFERFISH, 613, FishRarity.RARE);
        registerFish("evilfish", Items.TROPICAL_FISH, 667, FishRarity.RARE);
        registerFish("twinkfish", Items.SALMON, 702, FishRarity.RARE);
        registerFish("polargish", Items.COD, 743, FishRarity.RARE);
        registerFish("marksfish", Items.SALMON, 755, FishRarity.RARE);
        registerFish("cyberfish", Items.SALMON, 806, FishRarity.RARE);
        registerFish("sculk_infused_cod", Items.COD, 935, FishRarity.RARE);
        registerFish("sculk_infused_clownfish", Items.TROPICAL_FISH, 1012, FishRarity.RARE);
        registerFish("jenus", Items.COD, 1526, FishRarity.RARE);
        registerFish("deitumus", Items.SALMON, 1789, FishRarity.RARE);
        registerFish("sculk_infused_salmon", Items.SALMON, 1914, FishRarity.RARE);
        registerFish("sculk_infused_pufferfish", Items.PUFFERFISH, 2310, FishRarity.RARE);

        registerFish("slcoyd", Items.SALMON, 4200, FishRarity.EPIC);
        registerFish("trapezoin", Items.SALMON, 4800, FishRarity.EPIC);
        registerFish("pozmanze", Items.PUFFERFISH, 5000, FishRarity.EPIC);
        registerFish("mallecone", Items.COD, 5170, FishRarity.EPIC);
        registerFish("capisol", Items.COD, 5940, FishRarity.EPIC);
        registerFish("roluna", Items.PUFFERFISH, 5980, FishRarity.EPIC);
        registerFish("terrash", Items.TROPICAL_FISH, 6720, FishRarity.EPIC);
        registerFish("skool", Items.COD, 6940, FishRarity.EPIC);
        registerFish("sporfie", Items.PUFFERFISH, 8160, FishRarity.EPIC);
        registerFish("epicskillz", Items.TROPICAL_FISH, 9170, FishRarity.EPIC);
        registerFish("marine", Items.COD, 10120, FishRarity.EPIC);
        registerFish("hokona", Items.SALMON, 11840, FishRarity.EPIC);

        registerFish("savion", Items.COD, 16000, FishRarity.LEGENDARY);
        registerFish("miapan", Items.SALMON, 18200, FishRarity.LEGENDARY);
        registerFish("warvian", Items.COD, 22300, FishRarity.LEGENDARY);
        registerFish("poisonquill", Items.PUFFERFISH, 35700, FishRarity.LEGENDARY);
        registerFish("painted_cod", Items.TROPICAL_FISH, 40000, FishRarity.LEGENDARY);
        registerFish("auquavian", Items.COD, 57300, FishRarity.LEGENDARY);
        registerFish("memsh", Items.PUFFERFISH, 62400, FishRarity.LEGENDARY);
        registerFish("dravian", Items.SALMON, 75500, FishRarity.LEGENDARY);

        registerFish("cor", Items.TROPICAL_FISH, 95000, FishRarity.MYTHIC);
        registerFish("gra", Items.COD, 97500, FishRarity.MYTHIC);
        registerFish("vi", Items.SALMON, 98000, FishRarity.MYTHIC);
        registerFish("an", Items.PUFFERFISH, 100000, FishRarity.MYTHIC);

        registerFish("fish_de_ckc", Items.SALMON, 125000, FishRarity.CHROMATIC);
        registerFish("porealis", Items.PUFFERFISH, 150000, FishRarity.CHROMATIC);

        registerFish("moncavia", Items.COD, 250000, FishRarity.CELESTIAL);
    }
}
