package fun.spmc.smpmod;

import fun.spmc.smpmod.misc.ItemRarity;
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
    public static List<Item> DEFAULT_FISH = new ArrayList<>();

    private static <T extends Item> T create(String id, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smpmod", id));
        Item.Properties properties = new Item.Properties().setId(key);
        T item = factory.apply(properties);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static void registerRod(String id, RodTiers tier) {
        create(id, properties -> new RodItem(properties, tier));
    }

    private static void registerFish(String id, Item vanillaModel, double basePrice, ItemRarity rarity) {
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

        registerFish("cod", Items.COD, 15, ItemRarity.COMMON);
        registerFish("seafish", Items.COD, 21, ItemRarity.COMMON);
        registerFish("joes", Items.COD, 23, ItemRarity.COMMON);
        registerFish("salmon", Items.SALMON, 23, ItemRarity.COMMON);
        registerFish("valey", Items.SALMON, 24, ItemRarity.COMMON);
        registerFish("lilorange", Items.TROPICAL_FISH, 26, ItemRarity.COMMON);
        registerFish("coalbie", Items.SALMON, 27, ItemRarity.COMMON);
        registerFish("gegg", Items.PUFFERFISH, 29, ItemRarity.COMMON);
        registerFish("tropical_fish", Items.TROPICAL_FISH, 31, ItemRarity.COMMON);
        registerFish("tikira", Items.COD, 32, ItemRarity.COMMON);
        registerFish("red_bass", Items.SALMON, 33, ItemRarity.COMMON);
        registerFish("zombish", Items.SALMON, 34, ItemRarity.COMMON);
        registerFish("posie", Items.COD, 34, ItemRarity.COMMON);
        registerFish("mavide", Items.PUFFERFISH, 37, ItemRarity.COMMON);
        registerFish("porets", Items.SALMON, 38, ItemRarity.COMMON);
        registerFish("clownfish", Items.TROPICAL_FISH, 39, ItemRarity.COMMON);
        registerFish("pufferfish", Items.PUFFERFISH, 44, ItemRarity.COMMON);
        registerFish("tuffie", Items.PUFFERFISH, 46, ItemRarity.COMMON);
        registerFish("yoda", Items.SALMON, 48, ItemRarity.COMMON);
        registerFish("quelk", Items.PUFFERFISH, 48, ItemRarity.COMMON);
        registerFish("laugho", Items.TROPICAL_FISH, 49, ItemRarity.COMMON);
        registerFish("folk", Items.TROPICAL_FISH, 53, ItemRarity.COMMON);
        registerFish("plankton", Items.PUFFERFISH, 56, ItemRarity.COMMON);
        registerFish("chicken_jockey", Items.SALMON, 62, ItemRarity.COMMON);
        registerFish("cooper", Items.PUFFERFISH, 71, ItemRarity.COMMON);
        registerFish("sheldon", Items.COD, 83, ItemRarity.COMMON);
        registerFish("aure", Items.PUFFERFISH, 87, ItemRarity.COMMON);

        registerFish("rowlen", Items.COD, 97, ItemRarity.UNCOMMON);
        registerFish("skibindidle", Items.COD, 102, ItemRarity.UNCOMMON);
        registerFish("crazer", Items.SALMON, 117, ItemRarity.UNCOMMON);
        registerFish("venomfish", Items.PUFFERFISH, 183, ItemRarity.UNCOMMON);
        registerFish("enemyfish", Items.TROPICAL_FISH, 198, ItemRarity.UNCOMMON);
        registerFish("geysaurous", Items.SALMON, 237, ItemRarity.UNCOMMON);
        registerFish("jenfish", Items.COD, 316, ItemRarity.UNCOMMON);
        registerFish("tonka", Items.COD, 333, ItemRarity.UNCOMMON);
        registerFish("deitus", Items.PUFFERFISH, 362, ItemRarity.UNCOMMON);
        registerFish("mochafish", Items.TROPICAL_FISH, 414, ItemRarity.UNCOMMON);
        registerFish("diddus", Items.SALMON, 428, ItemRarity.UNCOMMON);
        registerFish("codded_salmon", Items.COD, 471, ItemRarity.UNCOMMON);
        registerFish("kungdus_khan", Items.COD, 513, ItemRarity.UNCOMMON);
        registerFish("swordfish", Items.TROPICAL_FISH, 592, ItemRarity.UNCOMMON);
        registerFish("finfishius", Items.COD, 611, ItemRarity.UNCOMMON);
        registerFish("phantomfish", Items.PUFFERFISH, 632, ItemRarity.UNCOMMON);

        registerFish("glandsouris", Items.SALMON, 527, ItemRarity.RARE);
        registerFish("jumpus", Items.PUFFERFISH, 613, ItemRarity.RARE);
        registerFish("evilfish", Items.TROPICAL_FISH, 667, ItemRarity.RARE);
        registerFish("twinkfish", Items.SALMON, 702, ItemRarity.RARE);
        registerFish("polargish", Items.COD, 743, ItemRarity.RARE);
        registerFish("marksfish", Items.SALMON, 755, ItemRarity.RARE);
        registerFish("cyberfish", Items.SALMON, 806, ItemRarity.RARE);
        registerFish("sculk_infused_cod", Items.COD, 935, ItemRarity.RARE);
        registerFish("sculk_infused_clownfish", Items.TROPICAL_FISH, 1012, ItemRarity.RARE);
        registerFish("jenus", Items.COD, 1526, ItemRarity.RARE);
        registerFish("deitumus", Items.SALMON, 1789, ItemRarity.RARE);
        registerFish("sculk_infused_salmon", Items.SALMON, 1914, ItemRarity.RARE);
        registerFish("sculk_infused_pufferfish", Items.PUFFERFISH, 2310, ItemRarity.RARE);

        registerFish("slcoyd", Items.SALMON, 4200, ItemRarity.EPIC);
        registerFish("trapezoin", Items.SALMON, 4800, ItemRarity.EPIC);
        registerFish("pozmanze", Items.PUFFERFISH, 5000, ItemRarity.EPIC);
        registerFish("mallecone", Items.COD, 5170, ItemRarity.EPIC);
        registerFish("capisol", Items.COD, 5940, ItemRarity.EPIC);
        registerFish("roluna", Items.PUFFERFISH, 5980, ItemRarity.EPIC);
        registerFish("terrash", Items.TROPICAL_FISH, 6720, ItemRarity.EPIC);
        registerFish("skool", Items.COD, 6940, ItemRarity.EPIC);
        registerFish("sporfie", Items.PUFFERFISH, 8160, ItemRarity.EPIC);
        registerFish("epic_skillz", Items.TROPICAL_FISH, 9170, ItemRarity.EPIC);
        registerFish("marine", Items.COD, 10120, ItemRarity.EPIC);
        registerFish("hokona", Items.SALMON, 11840, ItemRarity.EPIC);

        registerFish("savion", Items.COD, 16000, ItemRarity.LEGENDARY);
        registerFish("miapan", Items.SALMON, 18200, ItemRarity.LEGENDARY);
        registerFish("warvian", Items.COD, 22300, ItemRarity.LEGENDARY);
        registerFish("mosun", Items.PUFFERFISH, 30100, ItemRarity.LEGENDARY);
        registerFish("poisonquill", Items.PUFFERFISH, 35700, ItemRarity.LEGENDARY);
        registerFish("painted_cod", Items.TROPICAL_FISH, 40000, ItemRarity.LEGENDARY);
        registerFish("auquavian", Items.COD, 57300, ItemRarity.LEGENDARY);
        registerFish("memsh", Items.PUFFERFISH, 62400, ItemRarity.LEGENDARY);
        registerFish("dravian", Items.SALMON, 75500, ItemRarity.LEGENDARY);

        registerFish("cor", Items.TROPICAL_FISH, 95000, ItemRarity.MYTHIC);
        registerFish("gra", Items.COD, 97500, ItemRarity.MYTHIC);
        registerFish("vi", Items.SALMON, 98000, ItemRarity.MYTHIC);
        registerFish("an", Items.PUFFERFISH, 100000, ItemRarity.MYTHIC);

        registerFish("fish_de_ckc", Items.SALMON, 125000, ItemRarity.CHROMATIC);
        registerFish("porealis", Items.PUFFERFISH, 150000, ItemRarity.CHROMATIC);

        registerFish("moncavia", Items.COD, 250000, ItemRarity.CELESTIAL);
    }

    private static void registerBiomeSpecific() {
        // plains
        registerFish("crab", Items.SALMON, 17, ItemRarity.COMMON);
        registerFish("frog", Items.COD, 19, ItemRarity.COMMON);
        registerFish("bass", Items.COD, 22, ItemRarity.COMMON);
        registerFish("flounder", Items.COD, 27, ItemRarity.COMMON);
        registerFish("trout", Items.SALMON, 31, ItemRarity.COMMON);
        registerFish("herring", Items.TROPICAL_FISH, 36, ItemRarity.COMMON);
        registerFish("tuna", Items.COD, 41, ItemRarity.COMMON);
        registerFish("anchovy", Items.SALMON, 47, ItemRarity.COMMON);
        registerFish("red_snapper", Items.COD, 56, ItemRarity.COMMON);
        registerFish("blue_snapper", Items.SALMON, 62, ItemRarity.COMMON);
        registerFish("yellow_snapper", Items.TROPICAL_FISH, 68, ItemRarity.COMMON);
        registerFish("green_snapper", Items.PUFFERFISH, 74, ItemRarity.COMMON);
        registerFish("shrimp", Items.TROPICAL_FISH, 99, ItemRarity.UNCOMMON);
        registerFish("orbfish", Items.PUFFERFISH, 146, ItemRarity.UNCOMMON);
        registerFish("oyster", Items.PUFFERFISH, 185, ItemRarity.UNCOMMON);
        registerFish("slimefish", Items.TROPICAL_FISH, 235, ItemRarity.UNCOMMON);
        registerFish("wongfish", Items.COD, 278, ItemRarity.UNCOMMON);
        registerFish("cybofish", Items.SALMON, 330, ItemRarity.UNCOMMON);
        registerFish("errorfish", Items.PUFFERFISH, 404, ItemRarity.UNCOMMON);
        registerFish("barracuda", Items.COD, 540, ItemRarity.UNCOMMON);
        registerFish("bonefish", Items.COD, 702, ItemRarity.RARE);
        registerFish("piranha", Items.SALMON, 818, ItemRarity.RARE);
        registerFish("bananafish", Items.PUFFERFISH, 940, ItemRarity.RARE);
        registerFish("lyonfish", Items.COD, 1176, ItemRarity.RARE);
        registerFish("jellyfish", Items.TROPICAL_FISH, 1484, ItemRarity.RARE);
        registerFish("catfish", Items.SALMON, 1704, ItemRarity.RARE);
        registerFish("orwellfish", Items.COD, 1984, ItemRarity.RARE);
        registerFish("tungfish", Items.SALMON, 4270, ItemRarity.EPIC);
        registerFish("john_pork_fish", Items.TROPICAL_FISH, 5620, ItemRarity.EPIC);
        registerFish("toiletfish", Items.SALMON, 6020, ItemRarity.EPIC);
        registerFish("colognefish", Items.PUFFERFISH, 7230, ItemRarity.EPIC);
        registerFish("cutfish", Items.COD, 9550, ItemRarity.EPIC);
        registerFish("technofish", Items.COD, 1740, ItemRarity.EPIC);
        registerFish("tetra", Items.COD, 21900, ItemRarity.LEGENDARY);
        registerFish("sawtooth", Items.TROPICAL_FISH, 44700, ItemRarity.LEGENDARY);
        registerFish("sea_serpent", Items.SALMON, 63100, ItemRarity.LEGENDARY);
        registerFish("zephyr", Items.PUFFERFISH, 80800, ItemRarity.LEGENDARY);
        registerFish("red_herring", Items.SALMON, 96500, ItemRarity.MYTHIC);
        registerFish("the_fish_that_travelled_through_time", Items.SALMON, 99000, ItemRarity.MYTHIC);
        registerFish("storm", Items.COD, 135000, ItemRarity.CHROMATIC);
        registerFish("kraken", Items.TROPICAL_FISH, 275000, ItemRarity.CELESTIAL);
    }
}
