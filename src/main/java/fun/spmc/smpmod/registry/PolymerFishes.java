package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.fishing.BiomeCategory;
import fun.spmc.smpmod.fishing.FishItem;
import fun.spmc.smpmod.fishing.RodItem;
import fun.spmc.smpmod.fishing.RodTiers;
import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PolymerFishes {
    private static final Map<String, Item> FISH_REGISTRY = new HashMap<>();

    public static List<Item> FISH = new ArrayList<>();
    public static List<Item> PLAINS = new ArrayList<>();
    public static List<Item> TROPICAL = new ArrayList<>();
    public static List<Item> DESERT = new ArrayList<>();
    public static List<Item> SNOWY = new ArrayList<>();
    public static List<Item> LAVA = new ArrayList<>();
    public static List<Item> DEEP = new ArrayList<>();
    public static List<Item> END = new ArrayList<>();
    public static List<Item> SKY = new ArrayList<>();

    public static List<Item> getAllFish() {
        return Stream.of(FISH, PLAINS, TROPICAL, DESERT, SNOWY, LAVA, DEEP, END, SKY)
                .flatMap(List::stream)
                .distinct()
                .toList(); // immutable bruv
    }

    private static void registerRod(RodTiers tier) { PolymerRegistry.createItem(tier.name().toLowerCase() + "_fishing_rod", properties -> new RodItem(properties, tier)); }
    private static void registerFish(String id, Item vanillaModel, double basePrice, ItemRarity rarity, List<Item> listToBeAdded) {
        Item item = PolymerRegistry.createItem(id, properties -> new FishItem(properties, vanillaModel, MessageUtils.formatName(id), basePrice, rarity));
        listToBeAdded.add(item);
        FISH_REGISTRY.put(id, item);
    }

    public static Item getFish(String id) {
        Item item = FISH_REGISTRY.get(id);
        if (item == null) throw new IllegalArgumentException("Unknown fish ID: " + id);
        return item;
    }

    protected static void registerRods() {
        registerRod(RodTiers.NORMAL);
        registerRod(RodTiers.RAINBOW);
        registerRod(RodTiers.COPPER);
        registerRod(RodTiers.IRON);
        registerRod(RodTiers.GOLD);
        registerRod(RodTiers.EMERALD);
        registerRod(RodTiers.LUNA);
        registerRod(RodTiers.DIAMOND);
        registerRod(RodTiers.NETHERITE);
        registerRod(RodTiers.TOXIC);
        registerRod(RodTiers.DEATH);
        registerRod(RodTiers.AIR);
        registerRod(RodTiers.SEA);
        registerRod(RodTiers.FLICKERING);
        registerRod(RodTiers.CELESTIAL);
        registerRod(RodTiers.ELEMENTAL);
        registerRod(RodTiers.CTHULHU);
        registerRod(RodTiers.EVERYTHING);
    }

    protected static void registerFishes() {
        PolymerRegistry.createItem("corgravian", properties -> new FishItem(properties, Items.SALMON, "Corgravian", 1000000, ItemRarity.CELESTIAL));

        registerDefault();
        registerPlains();
        registerTropical();
        registerDesert();
        registerSnowy();
        registerLava();
        registerDeep();
        registerEnd();
        registerSky();

        BiomeCategory.initLookupMap();
    }
    private static void registerDefault() {
        registerFish("cod", Items.COD, 15, ItemRarity.COMMON, FISH);
        registerFish("seafish", Items.COD, 21, ItemRarity.COMMON, FISH);
        registerFish("joes", Items.COD, 23, ItemRarity.COMMON, FISH);
        registerFish("salmon", Items.SALMON, 23, ItemRarity.COMMON, FISH);
        registerFish("valey", Items.SALMON, 24, ItemRarity.COMMON, FISH);
        registerFish("lilorange", Items.TROPICAL_FISH, 26, ItemRarity.COMMON, FISH);
        registerFish("coalbie", Items.SALMON, 27, ItemRarity.COMMON, FISH);
        registerFish("gegg", Items.PUFFERFISH, 29, ItemRarity.COMMON, FISH);
        registerFish("tropical_fish", Items.TROPICAL_FISH, 31, ItemRarity.COMMON, FISH);
        registerFish("tikira", Items.COD, 32, ItemRarity.COMMON, FISH);
        registerFish("red_bass", Items.SALMON, 33, ItemRarity.COMMON, FISH);
        registerFish("zombish", Items.SALMON, 34, ItemRarity.COMMON, FISH);
        registerFish("posie", Items.COD, 34, ItemRarity.COMMON, FISH);
        registerFish("mavide", Items.PUFFERFISH, 37, ItemRarity.COMMON, FISH);
        registerFish("porets", Items.SALMON, 38, ItemRarity.COMMON, FISH);
        registerFish("clownfish", Items.TROPICAL_FISH, 39, ItemRarity.COMMON, FISH);
        registerFish("pufferfish", Items.PUFFERFISH, 44, ItemRarity.COMMON, FISH);
        registerFish("tuffie", Items.PUFFERFISH, 46, ItemRarity.COMMON, FISH);
        registerFish("yoda", Items.SALMON, 48, ItemRarity.COMMON, FISH);
        registerFish("quelk", Items.PUFFERFISH, 48, ItemRarity.COMMON, FISH);
        registerFish("laugho", Items.TROPICAL_FISH, 49, ItemRarity.COMMON, FISH);
        registerFish("folk", Items.TROPICAL_FISH, 53, ItemRarity.COMMON, FISH);
        registerFish("plankton", Items.PUFFERFISH, 56, ItemRarity.COMMON, FISH);
        registerFish("chicken_jockey", Items.SALMON, 62, ItemRarity.COMMON, FISH);
        registerFish("cooper", Items.PUFFERFISH, 71, ItemRarity.COMMON, FISH);
        registerFish("sheldon", Items.COD, 83, ItemRarity.COMMON, FISH);
        registerFish("aure", Items.PUFFERFISH, 87, ItemRarity.COMMON, FISH);

        registerFish("rowlen", Items.COD, 97, ItemRarity.UNCOMMON, FISH);
        registerFish("skibindidle", Items.COD, 102, ItemRarity.UNCOMMON, FISH);
        registerFish("crazer", Items.SALMON, 117, ItemRarity.UNCOMMON, FISH);
        registerFish("venomfish", Items.PUFFERFISH, 183, ItemRarity.UNCOMMON, FISH);
        registerFish("enemyfish", Items.TROPICAL_FISH, 198, ItemRarity.UNCOMMON, FISH);
        registerFish("geysaurous", Items.SALMON, 237, ItemRarity.UNCOMMON, FISH);
        registerFish("jenfish", Items.COD, 316, ItemRarity.UNCOMMON, FISH);
        registerFish("tonka", Items.COD, 333, ItemRarity.UNCOMMON, FISH);
        registerFish("deitus", Items.PUFFERFISH, 362, ItemRarity.UNCOMMON, FISH);
        registerFish("mochafish", Items.TROPICAL_FISH, 414, ItemRarity.UNCOMMON, FISH);
        registerFish("diddus", Items.SALMON, 428, ItemRarity.UNCOMMON, FISH);
        registerFish("codded_salmon", Items.COD, 471, ItemRarity.UNCOMMON, FISH);
        registerFish("kungdus_khan", Items.COD, 513, ItemRarity.UNCOMMON, FISH);
        registerFish("swordfish", Items.TROPICAL_FISH, 592, ItemRarity.UNCOMMON, FISH);
        registerFish("finfishius", Items.COD, 611, ItemRarity.UNCOMMON, FISH);
        registerFish("phantomfish", Items.PUFFERFISH, 632, ItemRarity.UNCOMMON, FISH);

        registerFish("glandsouris", Items.SALMON, 527, ItemRarity.RARE, FISH);
        registerFish("jumpus", Items.PUFFERFISH, 613, ItemRarity.RARE, FISH);
        registerFish("evilfish", Items.TROPICAL_FISH, 667, ItemRarity.RARE, FISH);
        registerFish("twinkfish", Items.SALMON, 702, ItemRarity.RARE, FISH);
        registerFish("polargish", Items.COD, 743, ItemRarity.RARE, FISH);
        registerFish("marksfish", Items.SALMON, 755, ItemRarity.RARE, FISH);
        registerFish("cyberfish", Items.SALMON, 806, ItemRarity.RARE, FISH);
        registerFish("sculk_infused_cod", Items.ECHO_SHARD, 935, ItemRarity.RARE, FISH);
        registerFish("sculk_infused_clownfish", Items.ECHO_SHARD, 1012, ItemRarity.RARE, FISH);
        registerFish("jenus", Items.COD, 1526, ItemRarity.RARE, FISH);
        registerFish("deitumus", Items.SALMON, 1789, ItemRarity.RARE, FISH);
        registerFish("sculk_infused_salmon", Items.ECHO_SHARD, 1914, ItemRarity.RARE, FISH);
        registerFish("sculk_infused_pufferfish", Items.ECHO_SHARD, 2310, ItemRarity.RARE, FISH);

        registerFish("slcoyd", Items.SALMON, 4200, ItemRarity.EPIC, FISH);
        registerFish("trapezoin", Items.SALMON, 4800, ItemRarity.EPIC, FISH);
        registerFish("pozmanze", Items.PUFFERFISH, 5000, ItemRarity.EPIC, FISH);
        registerFish("mallecone", Items.COD, 5170, ItemRarity.EPIC, FISH);
        registerFish("capisol", Items.COD, 5940, ItemRarity.EPIC, FISH);
        registerFish("roluna", Items.PUFFERFISH, 5980, ItemRarity.EPIC, FISH);
        registerFish("terrash", Items.TROPICAL_FISH, 6720, ItemRarity.EPIC, FISH);
        registerFish("skool", Items.COD, 6940, ItemRarity.EPIC, FISH);
        registerFish("sporfie", Items.PUFFERFISH, 8160, ItemRarity.EPIC, FISH);
        registerFish("epic_skillz", Items.TROPICAL_FISH, 9170, ItemRarity.EPIC, FISH);
        registerFish("marine", Items.COD, 10120, ItemRarity.EPIC, FISH);
        registerFish("hokona", Items.SALMON, 11840, ItemRarity.EPIC, FISH);

        registerFish("savion", Items.COD, 16000, ItemRarity.LEGENDARY, FISH);
        registerFish("miapan", Items.SALMON, 18200, ItemRarity.LEGENDARY, FISH);
        registerFish("warvian", Items.COD, 22300, ItemRarity.LEGENDARY, FISH);
        registerFish("mosun", Items.PUFFERFISH, 30100, ItemRarity.LEGENDARY, FISH);
        registerFish("poisonquill", Items.PUFFERFISH, 35700, ItemRarity.LEGENDARY, FISH);
        registerFish("painted_cod", Items.TROPICAL_FISH, 40000, ItemRarity.LEGENDARY, FISH);
        registerFish("auquavian", Items.COD, 57300, ItemRarity.LEGENDARY, FISH);
        registerFish("memsh", Items.PUFFERFISH, 62400, ItemRarity.LEGENDARY, FISH);
        registerFish("dravian", Items.SALMON, 75500, ItemRarity.LEGENDARY, FISH);

        registerFish("cor", Items.TROPICAL_FISH, 95000, ItemRarity.MYTHIC, FISH);
        registerFish("gra", Items.COD, 97500, ItemRarity.MYTHIC, FISH);
        registerFish("vi", Items.SALMON, 98000, ItemRarity.MYTHIC, FISH);
        registerFish("an", Items.PUFFERFISH, 100000, ItemRarity.MYTHIC, FISH);

        registerFish("fish_de_ckc", Items.SALMON, 125000, ItemRarity.CHROMATIC, FISH);
        registerFish("porealis", Items.PUFFERFISH, 150000, ItemRarity.CHROMATIC, FISH);

        registerFish("moncavia", Items.COD, 250000, ItemRarity.CELESTIAL, FISH);
    }
    private static void registerPlains() {
        registerFish("crab", Items.SALMON, 17, ItemRarity.COMMON, PLAINS);
        registerFish("frog", Items.COD, 19, ItemRarity.COMMON, PLAINS);
        registerFish("bass", Items.COD, 22, ItemRarity.COMMON, PLAINS);
        registerFish("flounder", Items.COD, 27, ItemRarity.COMMON, PLAINS);
        registerFish("trout", Items.SALMON, 31, ItemRarity.COMMON, PLAINS);
        registerFish("herring", Items.TROPICAL_FISH, 36, ItemRarity.COMMON, PLAINS);
        registerFish("tuna", Items.COD, 41, ItemRarity.COMMON, PLAINS);
        registerFish("anchovy", Items.SALMON, 47, ItemRarity.COMMON, PLAINS);
        registerFish("red_snapper", Items.COD, 56, ItemRarity.COMMON, PLAINS);
        registerFish("blue_snapper", Items.SALMON, 62, ItemRarity.COMMON, PLAINS);
        registerFish("yellow_snapper", Items.TROPICAL_FISH, 68, ItemRarity.COMMON, PLAINS);
        registerFish("green_snapper", Items.PUFFERFISH, 74, ItemRarity.COMMON, PLAINS);
        registerFish("shrimp", Items.TROPICAL_FISH, 99, ItemRarity.UNCOMMON, PLAINS);
        registerFish("orbfish", Items.PUFFERFISH, 146, ItemRarity.UNCOMMON, PLAINS);
        registerFish("oyster", Items.PUFFERFISH, 185, ItemRarity.UNCOMMON, PLAINS);
        registerFish("slimefish", Items.TROPICAL_FISH, 235, ItemRarity.UNCOMMON, PLAINS);
        registerFish("wongfish", Items.COD, 278, ItemRarity.UNCOMMON, PLAINS);
        registerFish("cybofish", Items.SALMON, 330, ItemRarity.UNCOMMON, PLAINS);
        registerFish("errorfish", Items.PUFFERFISH, 404, ItemRarity.UNCOMMON, PLAINS);
        registerFish("barracuda", Items.COD, 540, ItemRarity.UNCOMMON, PLAINS);
        registerFish("bonefish", Items.COD, 702, ItemRarity.RARE, PLAINS);
        registerFish("piranha", Items.SALMON, 818, ItemRarity.RARE, PLAINS);
        registerFish("bananafish", Items.PUFFERFISH, 940, ItemRarity.RARE, PLAINS);
        registerFish("lyonfish", Items.COD, 1176, ItemRarity.RARE, PLAINS);
        registerFish("jellyfish", Items.TROPICAL_FISH, 1484, ItemRarity.RARE, PLAINS);
        registerFish("catfish", Items.SALMON, 1704, ItemRarity.RARE, PLAINS);
        registerFish("orwellfish", Items.COD, 1984, ItemRarity.RARE, PLAINS);
        registerFish("tungfish", Items.SALMON, 4270, ItemRarity.EPIC, PLAINS);
        registerFish("porkfish", Items.TROPICAL_FISH, 5620, ItemRarity.EPIC, PLAINS);
        registerFish("toiletfish", Items.SALMON, 6020, ItemRarity.EPIC, PLAINS);
        registerFish("colognefish", Items.PUFFERFISH, 7230, ItemRarity.EPIC, PLAINS);
        registerFish("cutfish", Items.COD, 9550, ItemRarity.EPIC, PLAINS);
        registerFish("technofish", Items.COD, 1740, ItemRarity.EPIC, PLAINS);
        registerFish("tetra", Items.COD, 21900, ItemRarity.LEGENDARY, PLAINS);
        registerFish("sawtooth", Items.TROPICAL_FISH, 44700, ItemRarity.LEGENDARY, PLAINS);
        registerFish("sea_serpent", Items.SALMON, 63100, ItemRarity.LEGENDARY, PLAINS);
        registerFish("zephyr", Items.PUFFERFISH, 80800, ItemRarity.LEGENDARY, PLAINS);
        registerFish("red_herring", Items.SALMON, 96500, ItemRarity.MYTHIC, PLAINS);
        registerFish("storm", Items.COD, 135000, ItemRarity.CHROMATIC, PLAINS);
        registerFish("kraken", Items.TROPICAL_FISH, 275000, ItemRarity.CELESTIAL, PLAINS);
    }
    private static void registerTropical() {
        registerFish("gar", Items.COD, 12, ItemRarity.COMMON, TROPICAL);
        registerFish("pike", Items.TROPICAL_FISH, 16, ItemRarity.COMMON, TROPICAL);
        registerFish("bream", Items.SALMON, 21, ItemRarity.COMMON, TROPICAL);
        registerFish("grouper", Items.COD, 27, ItemRarity.COMMON, TROPICAL);
        registerFish("magikarp", Items.TROPICAL_FISH, 33, ItemRarity.COMMON, TROPICAL);
        registerFish("minnow", Items.COD, 45, ItemRarity.COMMON, TROPICAL);
        registerFish("carp", Items.SALMON, 57, ItemRarity.COMMON, TROPICAL);
        registerFish("stalin", Items.COD, 80, ItemRarity.COMMON, TROPICAL);
        registerFish("m", Items.PUFFERFISH, 93, ItemRarity.COMMON, TROPICAL);
        registerFish("fusilier", Items.COD, 122, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("yellowtail", Items.SALMON, 156, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("lobster", Items.TROPICAL_FISH, 221, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("femfish", Items.SALMON, 289, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("yearfish", Items.PUFFERFISH, 365, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("furry", Items.TROPICAL_FISH, 420, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("fhyaian", Items.SALMON, 555, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("koi", Items.TROPICAL_FISH, 685, ItemRarity.UNCOMMON, TROPICAL);
        registerFish("stingray", Items.COD, 720, ItemRarity.RARE, TROPICAL);
        registerFish("flying_fish", Items.COD, 777, ItemRarity.RARE, TROPICAL);
        registerFish("karlfish", Items.SALMON, 1020, ItemRarity.RARE, TROPICAL);
        registerFish("nickfish", Items.TROPICAL_FISH, 1212, ItemRarity.RARE, TROPICAL);
        registerFish("angelfish", Items.PUFFERFISH, 1450, ItemRarity.RARE, TROPICAL);
        registerFish("robofish", Items.SALMON, 1643, ItemRarity.RARE, TROPICAL);
        registerFish("harp", Items.TROPICAL_FISH, 1917, ItemRarity.RARE, TROPICAL);
        registerFish("pandatail", Items.COD, 4510, ItemRarity.EPIC, TROPICAL);
        registerFish("man_o_war", Items.TROPICAL_FISH, 7550, ItemRarity.EPIC, TROPICAL);
        registerFish("marlin", Items.COD, 8990, ItemRarity.EPIC, TROPICAL);
        registerFish("wyvern", Items.PUFFERFISH, 10660, ItemRarity.EPIC, TROPICAL);
        registerFish("3y3", Items.PUFFERFISH, 12000, ItemRarity.EPIC, TROPICAL);
        registerFish("aleph", Items.COD, 16500, ItemRarity.LEGENDARY, TROPICAL);
        registerFish("tox", Items.TROPICAL_FISH, 32500, ItemRarity.LEGENDARY, TROPICAL);
        registerFish("kingfish", Items.COD, 45500, ItemRarity.LEGENDARY, TROPICAL);
        registerFish("peridot", Items.SALMON, 61500, ItemRarity.LEGENDARY, TROPICAL);
        registerFish("diastima", Items.COD, 95000, ItemRarity.MYTHIC, TROPICAL);
        registerFish("thalassa", Items.SALMON, 102500, ItemRarity.MYTHIC, TROPICAL);
        registerFish("asteri", Items.TROPICAL_FISH, 162500, ItemRarity.CHROMATIC, TROPICAL);
        registerFish("leviathan", Items.PUFFERFISH, 300000, ItemRarity.CELESTIAL, TROPICAL);
    }
    private static void registerDesert() {
        registerFish("sandy", Items.SALMON, 18, ItemRarity.COMMON, DESERT);
        registerFish("tadpole", Items.TROPICAL_FISH, 22, ItemRarity.COMMON, DESERT);
        registerFish("cactail", Items.SALMON, 28, ItemRarity.COMMON, DESERT);
        registerFish("scarab", Items.PUFFERFISH, 35, ItemRarity.COMMON, DESERT);
        registerFish("spotted_salmon", Items.COD, 41, ItemRarity.COMMON, DESERT);
        registerFish("pinfish", Items.SALMON, 48, ItemRarity.COMMON, DESERT);
        registerFish("scissortail", Items.COD, 56, ItemRarity.COMMON, DESERT);
        registerFish("alligator", Items.SALMON, 62, ItemRarity.COMMON, DESERT);
        registerFish("canetail", Items.PUFFERFISH, 71, ItemRarity.COMMON, DESERT);
        registerFish("miguelius", Items.TROPICAL_FISH, 72, ItemRarity.COMMON, DESERT);
        registerFish("wrasse", Items.PUFFERFISH, 76, ItemRarity.COMMON, DESERT);
        registerFish("miserium", Items.TROPICAL_FISH, 82, ItemRarity.COMMON, DESERT);
        registerFish("scorpion", Items.COD, 104, ItemRarity.UNCOMMON, DESERT);
        registerFish("dolphin", Items.SALMON, 153, ItemRarity.UNCOMMON, DESERT);
        registerFish("bombtail", Items.TROPICAL_FISH, 189, ItemRarity.UNCOMMON, DESERT);
        registerFish("rustfin", Items.COD, 243, ItemRarity.UNCOMMON, DESERT);
        registerFish("sheath", Items.SALMON, 298, ItemRarity.UNCOMMON, DESERT);
        registerFish("sunbaked", Items.COD, 341, ItemRarity.UNCOMMON, DESERT);
        registerFish("mackerel", Items.SALMON, 465, ItemRarity.UNCOMMON, DESERT);
        registerFish("goby", Items.SALMON, 544, ItemRarity.UNCOMMON, DESERT);
        registerFish("arkfish", Items.PUFFERFISH, 610, ItemRarity.RARE, DESERT);
        registerFish("mossfish", Items.PUFFERFISH, 829, ItemRarity.RARE, DESERT);
        registerFish("samtail", Items.TROPICAL_FISH, 999, ItemRarity.RARE, DESERT);
        registerFish("c4", Items.TROPICAL_FISH, 1375, ItemRarity.RARE, DESERT);
        registerFish("sunfish", Items.TROPICAL_FISH, 1620, ItemRarity.RARE, DESERT);
        registerFish("fish_o_tron", Items.SALMON, 1809, ItemRarity.RARE, DESERT);
        registerFish("ambertail", Items.TROPICAL_FISH, 4560, ItemRarity.EPIC, DESERT);
        registerFish("starfish", Items.TROPICAL_FISH, 5580, ItemRarity.EPIC, DESERT);
        registerFish("pinkfog", Items.COD, 7980, ItemRarity.EPIC, DESERT);
        registerFish("dynafish", Items.COD, 9670, ItemRarity.EPIC, DESERT);
        registerFish("boreal", Items.PUFFERFISH, 11010, ItemRarity.EPIC, DESERT);
        registerFish("orca", Items.COD, 12130, ItemRarity.EPIC, DESERT);
        registerFish("krypt", Items.SALMON, 17200, ItemRarity.LEGENDARY, DESERT);
        registerFish("mirage", Items.COD, 24500, ItemRarity.LEGENDARY, DESERT);
        registerFish("stargaze", Items.TROPICAL_FISH, 43500, ItemRarity.LEGENDARY, DESERT);
        registerFish("reaper", Items.SALMON, 68500, ItemRarity.LEGENDARY, DESERT);
        registerFish("zeus", Items.PUFFERFISH, 92500, ItemRarity.MYTHIC, DESERT);
        registerFish("prismite", Items.PUFFERFISH, 155000, ItemRarity.CHROMATIC, DESERT);
        registerFish("megalodon", Items.SALMON, 262500, ItemRarity.CELESTIAL, DESERT);

    }
    private static void registerSnowy() {
        registerFish("eel", Items.SALMON, 15, ItemRarity.COMMON, SNOWY);
        registerFish("icefish", Items.COD, 20, ItemRarity.COMMON, SNOWY);
        registerFish("saury", Items.SALMON, 24, ItemRarity.COMMON, SNOWY);
        registerFish("cusk", Items.COD, 28, ItemRarity.COMMON, SNOWY);
        registerFish("haddock", Items.COD, 32, ItemRarity.COMMON, SNOWY);
        registerFish("swoosh", Items.SALMON, 36, ItemRarity.COMMON, SNOWY);
        registerFish("penguin", Items.SALMON, 42, ItemRarity.COMMON, SNOWY);
        registerFish("albacore", Items.SALMON, 47, ItemRarity.COMMON, SNOWY);
        registerFish("lungfish", Items.SALMON, 54, ItemRarity.COMMON, SNOWY);
        registerFish("sardine", Items.SALMON, 58, ItemRarity.COMMON, SNOWY);
        registerFish("frost_minnow", Items.SALMON, 64, ItemRarity.COMMON, SNOWY);
        registerFish("polar_bear", Items.SALMON, 90, ItemRarity.UNCOMMON, SNOWY);
        registerFish("viperfish", Items.COD, 176, ItemRarity.UNCOMMON, SNOWY);
        registerFish("bluetang", Items.COD, 256, ItemRarity.UNCOMMON, SNOWY);
        registerFish("idfish", Items.COD, 298, ItemRarity.UNCOMMON, SNOWY);
        registerFish("clam", Items.TROPICAL_FISH, 361, ItemRarity.UNCOMMON, SNOWY);
        registerFish("byronne", Items.TROPICAL_FISH, 419, ItemRarity.UNCOMMON, SNOWY);
        registerFish("gugutang", Items.TROPICAL_FISH, 505, ItemRarity.UNCOMMON, SNOWY);
        registerFish("sculk_infused_moonfish", Items.ECHO_SHARD, 706, ItemRarity.RARE, SNOWY);
        registerFish("electric_eel", Items.SALMON, 882, ItemRarity.RARE, SNOWY);
        registerFish("lionfish", Items.TROPICAL_FISH, 925, ItemRarity.RARE, SNOWY);
        registerFish("axolotl", Items.PUFFERFISH, 1180, ItemRarity.RARE, SNOWY);
        registerFish("glassfish", Items.PUFFERFISH, 1383, ItemRarity.RARE, SNOWY);
        registerFish("gallina", Items.PUFFERFISH, 1598, ItemRarity.RARE, SNOWY);
        registerFish("glace", Items.COD, 1808, ItemRarity.RARE, SNOWY);
        registerFish("rainbow_trout", Items.PUFFERFISH, 4110, ItemRarity.EPIC, SNOWY);
        registerFish("sabertooth", Items.SALMON, 5060, ItemRarity.EPIC, SNOWY);
        registerFish("manta_ray", Items.TROPICAL_FISH, 7280, ItemRarity.EPIC, SNOWY);
        registerFish("mako", Items.SALMON, 8650, ItemRarity.EPIC, SNOWY);
        registerFish("beluga", Items.COD, 10560, ItemRarity.EPIC, SNOWY);
        registerFish("berg", Items.COD, 13100, ItemRarity.EPIC, SNOWY);
        registerFish("painted_salmon", Items.COD, 18800, ItemRarity.LEGENDARY, SNOWY);
        registerFish("sculk_infused_chimera", Items.ECHO_SHARD, 34200, ItemRarity.LEGENDARY, SNOWY);
        registerFish("thresher", Items.SALMON, 51600, ItemRarity.LEGENDARY, SNOWY);
        registerFish("great_white_shark", Items.SALMON, 72300, ItemRarity.LEGENDARY, SNOWY);
        registerFish("poseidon", Items.COD, 82500, ItemRarity.MYTHIC, SNOWY);
        registerFish("hydra", Items.SALMON, 105000, ItemRarity.MYTHIC, SNOWY);
        registerFish("yeti", Items.SALMON, 145000, ItemRarity.CHROMATIC, SNOWY);
        registerFish("bahamut", Items.PUFFERFISH, 287500, ItemRarity.CELESTIAL, SNOWY);
    }
    private static void registerLava() {
        registerFish("mahi_mahi", Items.SALMON, 14, ItemRarity.COMMON, LAVA);
        registerFish("flamecrab", Items.COD, 18, ItemRarity.COMMON, LAVA);
        registerFish("lava_snail", Items.PUFFERFISH, 24, ItemRarity.COMMON, LAVA);
        registerFish("firefin", Items.PUFFERFISH, 33, ItemRarity.COMMON, LAVA);
        registerFish("obsidine", Items.SALMON, 49, ItemRarity.COMMON, LAVA);
        registerFish("strider", Items.SALMON, 53, ItemRarity.COMMON, LAVA);
        registerFish("magma_cube", Items.TROPICAL_FISH, 62, ItemRarity.COMMON, LAVA);
        registerFish("pigfish", Items.PUFFERFISH, 66, ItemRarity.COMMON, LAVA);
        registerFish("basalfin", Items.COD, 70, ItemRarity.COMMON, LAVA);
        registerFish("blacktang", Items.SALMON, 74, ItemRarity.COMMON, LAVA);
        registerFish("pyroleech", Items.PUFFERFISH, 76, ItemRarity.COMMON, LAVA);
        registerFish("bloodfin", Items.PUFFERFISH, 80, ItemRarity.COMMON, LAVA);
        registerFish("spiderfish", Items.SALMON, 120, ItemRarity.UNCOMMON, LAVA);
        registerFish("grenadier", Items.COD, 152, ItemRarity.UNCOMMON, LAVA);
        registerFish("magmatang", Items.PUFFERFISH, 214, ItemRarity.UNCOMMON, LAVA);
        registerFish("forgefin", Items.PUFFERFISH, 271, ItemRarity.UNCOMMON, LAVA);
        registerFish("scorchmel", Items.PUFFERFISH, 349, ItemRarity.UNCOMMON, LAVA);
        registerFish("bruisette", Items.SALMON, 464, ItemRarity.UNCOMMON, LAVA);
        registerFish("lacera", Items.TROPICAL_FISH, 512, ItemRarity.UNCOMMON, LAVA);
        registerFish("sinis", Items.SALMON, 576, ItemRarity.UNCOMMON, LAVA);
        registerFish("ichorfish", Items.PUFFERFISH, 620, ItemRarity.RARE, LAVA);
        registerFish("flarefin", Items.PUFFERFISH, 892, ItemRarity.RARE, LAVA);
        registerFish("wartfin", Items.SALMON, 1014, ItemRarity.RARE, LAVA);
        registerFish("magnalav", Items.PUFFERFISH, 1285, ItemRarity.RARE, LAVA);
        registerFish("volcan", Items.COD, 1483, ItemRarity.RARE, LAVA);
        registerFish("ghoul", Items.TROPICAL_FISH, 1671, ItemRarity.RARE, LAVA);
        registerFish("blazier", Items.PUFFERFISH, 2041, ItemRarity.RARE, LAVA);
        registerFish("hemopsari", Items.PUFFERFISH, 4570, ItemRarity.EPIC, LAVA);
        registerFish("pyroxene", Items.COD, 5740, ItemRarity.EPIC, LAVA);
        registerFish("tephra", Items.SALMON, 7530, ItemRarity.EPIC, LAVA);
        registerFish("broken_soul", Items.TROPICAL_FISH, 9660, ItemRarity.EPIC, LAVA);
        registerFish("finferno", Items.PUFFERFISH, 10280, ItemRarity.EPIC, LAVA);
        registerFish("purgaton", Items.SALMON, 12200, ItemRarity.EPIC, LAVA);
        registerFish("painted_tropical_fish", Items.PUFFERFISH, 23200, ItemRarity.LEGENDARY, LAVA);
        registerFish("illumi", Items.PUFFERFISH, 44100, ItemRarity.LEGENDARY, LAVA);
        registerFish("molt", Items.PUFFERFISH, 65800, ItemRarity.LEGENDARY, LAVA);
        registerFish("kuudra", Items.COD, 72400, ItemRarity.LEGENDARY, LAVA);
        registerFish("hades", Items.COD, 87500, ItemRarity.MYTHIC, LAVA);
        registerFish("xenomorph", Items.SALMON, 97500, ItemRarity.MYTHIC, LAVA);
        registerFish("beelzebub", Items.COD, 170000, ItemRarity.CHROMATIC, LAVA);
        registerFish("cthulhu", Items.COD, 312500, ItemRarity.CELESTIAL, LAVA);

    }
    private static void registerDeep() {
        registerFish("rockfish", Items.SALMON, 17, ItemRarity.COMMON, DEEP);
        registerFish("stonefish", Items.COD, 19, ItemRarity.COMMON, DEEP);
        registerFish("halibut", Items.SALMON, 22, ItemRarity.COMMON, DEEP);
        registerFish("andera", Items.SALMON, 25, ItemRarity.COMMON, DEEP);
        registerFish("coalcarp", Items.COD, 30, ItemRarity.COMMON, DEEP);
        registerFish("copperbit", Items.COD, 37, ItemRarity.COMMON, DEEP);
        registerFish("granisie", Items.COD, 44, ItemRarity.COMMON, DEEP);
        registerFish("dioritan", Items.SALMON, 50, ItemRarity.COMMON, DEEP);
        registerFish("silverfish", Items.SALMON, 53, ItemRarity.COMMON, DEEP);
        registerFish("claylen", Items.SALMON, 59, ItemRarity.COMMON, DEEP);
        registerFish("gravene", Items.SALMON, 67, ItemRarity.COMMON, DEEP);
        registerFish("lanternfish", Items.TROPICAL_FISH, 74, ItemRarity.COMMON, DEEP);
        registerFish("glowlight_danio", Items.TROPICAL_FISH, 88, ItemRarity.UNCOMMON, DEEP);
        registerFish("quartzray", Items.TROPICAL_FISH, 142, ItemRarity.UNCOMMON, DEEP);
        registerFish("sardiron", Items.SALMON, 202, ItemRarity.UNCOMMON, DEEP);
        registerFish("bronslate", Items.PUFFERFISH, 276, ItemRarity.UNCOMMON, DEEP);
        registerFish("feldspear", Items.PUFFERFISH, 369, ItemRarity.UNCOMMON, DEEP);
        registerFish("vanesse", Items.PUFFERFISH, 440, ItemRarity.UNCOMMON, DEEP);
        registerFish("wilsine", Items.PUFFERFISH, 494, ItemRarity.UNCOMMON, DEEP);
        registerFish("tuffffin", Items.TROPICAL_FISH, 554, ItemRarity.UNCOMMON, DEEP);
        registerFish("moai", Items.SALMON, 603, ItemRarity.RARE, DEEP);
        registerFish("lapine", Items.PUFFERFISH, 793, ItemRarity.RARE, DEEP);
        registerFish("redstil", Items.PUFFERFISH, 975, ItemRarity.RARE, DEEP);
        registerFish("prismaray", Items.COD, 1207, ItemRarity.RARE, DEEP);
        registerFish("ametang", Items.TROPICAL_FISH, 1436, ItemRarity.RARE, DEEP);
        registerFish("photopectoralis", Items.TROPICAL_FISH, 1732, ItemRarity.RARE, DEEP);
        registerFish("cryster", Items.TROPICAL_FISH, 1941, ItemRarity.RARE, DEEP);
        registerFish("anglerfish", Items.TROPICAL_FISH, 5620, ItemRarity.EPIC, DEEP);
        registerFish("frogold", Items.TROPICAL_FISH, 6860, ItemRarity.EPIC, DEEP);
        registerFish("emerafin", Items.SALMON, 8300, ItemRarity.EPIC, DEEP);
        registerFish("bedroam", Items.SALMON, 10250, ItemRarity.EPIC, DEEP);
        registerFish("minerafin", Items.PUFFERFISH, 11430, ItemRarity.EPIC, DEEP);
        registerFish("sahur", Items.PUFFERFISH, 12750, ItemRarity.EPIC, DEEP);
        registerFish("sierra", Items.COD, 25800, ItemRarity.LEGENDARY, DEEP);
        registerFish("diamondfish", Items.SALMON, 39800, ItemRarity.LEGENDARY, DEEP);
        registerFish("cyclops", Items.SALMON, 62400, ItemRarity.LEGENDARY, DEEP);
        registerFish("rascal", Items.COD, 83000, ItemRarity.LEGENDARY, DEEP);
        registerFish("gaia", Items.PUFFERFISH, 94000, ItemRarity.MYTHIC, DEEP);
        registerFish("scylla", Items.PUFFERFISH, 106000, ItemRarity.MYTHIC, DEEP);
        registerFish("sentinel", Items.SALMON, 175000, ItemRarity.CHROMATIC, DEEP);
        registerFish("bakunawa", Items.PUFFERFISH, 325000, ItemRarity.CELESTIAL, DEEP);
    }
    private static void registerEnd() {
        registerFish("tilapia", Items.STAINED_GLASS_PANE.lightGray(), 17, ItemRarity.COMMON, END);
        registerFish("endermite", Items.STAINED_GLASS_PANE.lightGray(), 19, ItemRarity.COMMON, END);
        registerFish("voidling", Items.STAINED_GLASS_PANE.lightGray(), 23, ItemRarity.COMMON, END);
        registerFish("oxyene", Items.STAINED_GLASS_PANE.lightGray(), 29, ItemRarity.COMMON, END);
        registerFish("endon", Items.STAINED_GLASS_PANE.lightGray(), 37, ItemRarity.COMMON, END);
        registerFish("wastefin", Items.STAINED_GLASS_PANE.lightGray(), 41, ItemRarity.COMMON, END);
        registerFish("barrere", Items.STAINED_GLASS_PANE.lightGray(), 46, ItemRarity.COMMON, END);
        registerFish("limbo", Items.STAINED_GLASS_PANE.lightGray(), 51, ItemRarity.COMMON, END);
        registerFish("sans", Items.STAINED_GLASS_PANE.lightGray(), 57, ItemRarity.COMMON, END);
        registerFish("piscis", Items.STAINED_GLASS_PANE.lightGray(), 63, ItemRarity.COMMON, END);
        registerFish("inane", Items.STAINED_GLASS_PANE.lightGray(), 69, ItemRarity.COMMON, END);
        registerFish("terminus", Items.STAINED_GLASS_PANE.lightGray(), 74, ItemRarity.COMMON, END);
        registerFish("shulke", Items.STAINED_GLASS_PANE.lightGray(), 91, ItemRarity.UNCOMMON, END);
        registerFish("illusionelle", Items.STAINED_GLASS_PANE.lightGray(), 113, ItemRarity.UNCOMMON, END);
        registerFish("vacutail", Items.STAINED_GLASS_PANE.lightGray(), 190, ItemRarity.UNCOMMON, END);
        registerFish("lepisma", Items.STAINED_GLASS_PANE.lightGray(), 341, ItemRarity.UNCOMMON, END);
        registerFish("cosmo", Items.STAINED_GLASS_PANE.lightGray(), 465, ItemRarity.UNCOMMON, END);
        registerFish("senza", Items.STAINED_GLASS_PANE.lightGray(), 527, ItemRarity.UNCOMMON, END);
        registerFish("nihil", Items.STAINED_GLASS_PANE.lightGray(), 591, ItemRarity.UNCOMMON, END);
        registerFish("vocivus", Items.STAINED_GLASS_PANE.lightGray(), 645, ItemRarity.UNCOMMON, END);
        registerFish("moonfish", Items.STAINED_GLASS_PANE.lightGray(), 813, ItemRarity.RARE, END);
        registerFish("levifin", Items.STAINED_GLASS_PANE.lightGray(), 1070, ItemRarity.RARE, END);
        registerFish("petrichor", Items.STAINED_GLASS_PANE.lightGray(), 1310, ItemRarity.RARE, END);
        registerFish("choru", Items.STAINED_GLASS_PANE.lightGray(), 1682, ItemRarity.RARE, END);
        registerFish("aeon", Items.STAINED_GLASS_PANE.lightGray(), 1904, ItemRarity.RARE, END);
        registerFish("planetario", Items.STAINED_GLASS_PANE.lightGray(), 2133, ItemRarity.RARE, END);
        registerFish("divane", Items.STAINED_GLASS_PANE.lightGray(), 2452, ItemRarity.RARE, END);
        registerFish("dragonfish", Items.STAINED_GLASS_PANE.lightGray(), 4444, ItemRarity.EPIC, END);
        registerFish("abyssa", Items.STAINED_GLASS_PANE.lightGray(), 5555, ItemRarity.EPIC, END);
        registerFish("hollowan", Items.STAINED_GLASS_PANE.lightGray(), 7777, ItemRarity.EPIC, END);
        registerFish("4o4", Items.STAINED_GLASS_PANE.lightGray(), 9999, ItemRarity.EPIC, END);
        registerFish("corruptin", Items.STAINED_GLASS_PANE.lightGray(), 11111, ItemRarity.EPIC, END);
        registerFish("andromeda", Items.STAINED_GLASS_PANE.lightGray(), 12321, ItemRarity.EPIC, END);
        registerFish("painted_pufferfish", Items.STAINED_GLASS_PANE.lightGray(), 22222, ItemRarity.LEGENDARY, END);
        registerFish("empty", Items.STAINED_GLASS_PANE.lightGray(), 33333, ItemRarity.LEGENDARY, END);
        registerFish("hex0101coda", Items.STAINED_GLASS_PANE.lightGray(), 50000, ItemRarity.LEGENDARY, END);
        registerFish("infinity", Items.STAINED_GLASS_PANE.lightGray(), 77777, ItemRarity.LEGENDARY, END);
        registerFish("undefined", Items.STAINED_GLASS_PANE.lightGray(), 99999, ItemRarity.MYTHIC, END);
        registerFish("null", Items.STAINED_GLASS_PANE.lightGray(), 111111, ItemRarity.MYTHIC, END);
        registerFish("galax", Items.STAINED_GLASS_PANE.lightGray(), 171717, ItemRarity.CHROMATIC, END);
        registerFish("ningen", Items.STAINED_GLASS_PANE.lightGray(), 337500, ItemRarity.CELESTIAL, END);
    }
    private static void registerSky() {
        registerFish("pollock", Items.SALMON, 24, ItemRarity.COMMON, SKY);
        registerFish("goldfish", Items.COD, 30, ItemRarity.COMMON, SKY);
        registerFish("perch", Items.SALMON, 38, ItemRarity.COMMON, SKY);
        registerFish("turtle", Items.TROPICAL_FISH, 46, ItemRarity.COMMON, SKY);
        registerFish("wave", Items.PUFFERFISH, 51, ItemRarity.COMMON, SKY);
        registerFish("nestie", Items.TROPICAL_FISH, 58, ItemRarity.COMMON, SKY);
        registerFish("mesher", Items.SALMON, 71, ItemRarity.COMMON, SKY);
        registerFish("avia", Items.SALMON, 76, ItemRarity.COMMON, SKY);
        registerFish("starlie", Items.TROPICAL_FISH, 83, ItemRarity.COMMON, SKY);
        registerFish("aerofin", Items.COD, 91, ItemRarity.COMMON, SKY);
        registerFish("cawl", Items.COD, 99, ItemRarity.COMMON, SKY);
        registerFish("vultan", Items.COD, 101, ItemRarity.COMMON, SKY);
        registerFish("cloudfish", Items.SALMON, 156, ItemRarity.UNCOMMON, SKY);
        registerFish("seahorse", Items.PUFFERFISH, 236, ItemRarity.UNCOMMON, SKY);
        registerFish("swanzie", Items.SALMON, 303, ItemRarity.UNCOMMON, SKY);
        registerFish("leape", Items.SALMON, 379, ItemRarity.UNCOMMON, SKY);
        registerFish("flamingo", Items.PUFFERFISH, 481, ItemRarity.UNCOMMON, SKY);
        registerFish("windle", Items.SALMON, 593, ItemRarity.UNCOMMON, SKY);
        registerFish("cutskydiver", Items.TROPICAL_FISH, 675, ItemRarity.UNCOMMON, SKY);
        registerFish("mistie", Items.SALMON, 777, ItemRarity.UNCOMMON, SKY);
        registerFish("sailfish", Items.COD, 908, ItemRarity.RARE, SKY);
        registerFish("vaporfin", Items.SALMON, 1142, ItemRarity.RARE, SKY);
        registerFish("hazy", Items.SALMON, 1303, ItemRarity.RARE, SKY);
        registerFish("nitron", Items.PUFFERFISH, 1622, ItemRarity.RARE, SKY);
        registerFish("aria", Items.SALMON, 1965, ItemRarity.RARE, SKY);
        registerFish("john", Items.COD, 2340, ItemRarity.RARE, SKY);
        registerFish("caelum", Items.SALMON, 2706, ItemRarity.RARE, SKY);
        registerFish("altus", Items.SALMON, 5160, ItemRarity.EPIC, SKY);
        registerFish("aura", Items.SALMON, 9340, ItemRarity.EPIC, SKY);
        registerFish("soul", Items.PUFFERFISH, 12350, ItemRarity.EPIC, SKY);
        registerFish("nebulo", Items.SALMON, 14040, ItemRarity.EPIC, SKY);
        registerFish("holine", Items.TROPICAL_FISH, 16800, ItemRarity.EPIC, SKY);
        registerFish("spatia", Items.SALMON, 19200, ItemRarity.EPIC, SKY);
        registerFish("aerie", Items.SALMON, 31300, ItemRarity.LEGENDARY, SKY);
        registerFish("unicorn", Items.TROPICAL_FISH, 54600, ItemRarity.LEGENDARY, SKY);
        registerFish("aether", Items.SALMON, 79300, ItemRarity.LEGENDARY, SKY);
        registerFish("arceus", Items.TROPICAL_FISH, 92500, ItemRarity.LEGENDARY, SKY);
        registerFish("elysia", Items.TROPICAL_FISH, 110000, ItemRarity.MYTHIC, SKY);
        registerFish("sylph", Items.PUFFERFISH, 125000, ItemRarity.MYTHIC, SKY);
        registerFish("icarus", Items.SALMON, 180000, ItemRarity.CHROMATIC, SKY);
        registerFish("lusca", Items.TROPICAL_FISH, 350000, ItemRarity.CELESTIAL, SKY);
    }
}
