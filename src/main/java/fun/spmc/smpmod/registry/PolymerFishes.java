package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.fishing.FishItem;
import fun.spmc.smpmod.fishing.RodItem;
import fun.spmc.smpmod.fishing.RodTiers;
import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PolymerFishes {
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
                .toList(); // immutable bruv
    }

    private static void registerRod(RodTiers tier) { PolymerRegistry.createItem(tier.name().toLowerCase() + "_fishing_rod", properties -> new RodItem(properties, tier)); }
    private static void registerFish(String id, Item vanillaModel, double basePrice, ItemRarity rarity) {
        Item item = PolymerRegistry.createItem(id, properties -> new FishItem(properties, vanillaModel, MessageUtils.formatName(id), basePrice, rarity));
        FISH.add(item);
    }

    private static void registerBiomeFish(String id, Item vanillaModel, double basePrice, ItemRarity rarity, List<Item> listToBeAdded) {
        Item item = PolymerRegistry.createItem(id, properties -> new FishItem(properties, vanillaModel, MessageUtils.formatName(id), basePrice, rarity));
        listToBeAdded.add(item);
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
    }
    private static void registerDefault() {
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
        registerFish("sculk_infused_cod", Items.ECHO_SHARD, 935, ItemRarity.RARE);
        registerFish("sculk_infused_clownfish", Items.ECHO_SHARD, 1012, ItemRarity.RARE);
        registerFish("jenus", Items.COD, 1526, ItemRarity.RARE);
        registerFish("deitumus", Items.SALMON, 1789, ItemRarity.RARE);
        registerFish("sculk_infused_salmon", Items.ECHO_SHARD, 1914, ItemRarity.RARE);
        registerFish("sculk_infused_pufferfish", Items.ECHO_SHARD, 2310, ItemRarity.RARE);

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
    private static void registerPlains() {
        registerBiomeFish("crab", Items.SALMON, 17, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("frog", Items.COD, 19, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("bass", Items.COD, 22, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("flounder", Items.COD, 27, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("trout", Items.SALMON, 31, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("herring", Items.TROPICAL_FISH, 36, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("tuna", Items.COD, 41, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("anchovy", Items.SALMON, 47, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("red_snapper", Items.COD, 56, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("blue_snapper", Items.SALMON, 62, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("yellow_snapper", Items.TROPICAL_FISH, 68, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("green_snapper", Items.PUFFERFISH, 74, ItemRarity.COMMON, PLAINS);
        registerBiomeFish("shrimp", Items.TROPICAL_FISH, 99, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("orbfish", Items.PUFFERFISH, 146, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("oyster", Items.PUFFERFISH, 185, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("slimefish", Items.TROPICAL_FISH, 235, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("wongfish", Items.COD, 278, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("cybofish", Items.SALMON, 330, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("errorfish", Items.PUFFERFISH, 404, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("barracuda", Items.COD, 540, ItemRarity.UNCOMMON, PLAINS);
        registerBiomeFish("bonefish", Items.COD, 702, ItemRarity.RARE, PLAINS);
        registerBiomeFish("piranha", Items.SALMON, 818, ItemRarity.RARE, PLAINS);
        registerBiomeFish("bananafish", Items.PUFFERFISH, 940, ItemRarity.RARE, PLAINS);
        registerBiomeFish("lyonfish", Items.COD, 1176, ItemRarity.RARE, PLAINS);
        registerBiomeFish("jellyfish", Items.TROPICAL_FISH, 1484, ItemRarity.RARE, PLAINS);
        registerBiomeFish("catfish", Items.SALMON, 1704, ItemRarity.RARE, PLAINS);
        registerBiomeFish("orwellfish", Items.COD, 1984, ItemRarity.RARE, PLAINS);
        registerBiomeFish("tungfish", Items.SALMON, 4270, ItemRarity.EPIC, PLAINS);
        registerBiomeFish("porkfish", Items.TROPICAL_FISH, 5620, ItemRarity.EPIC, PLAINS);
        registerBiomeFish("toiletfish", Items.SALMON, 6020, ItemRarity.EPIC, PLAINS);
        registerBiomeFish("colognefish", Items.PUFFERFISH, 7230, ItemRarity.EPIC, PLAINS);
        registerBiomeFish("cutfish", Items.COD, 9550, ItemRarity.EPIC, PLAINS);
        registerBiomeFish("technofish", Items.COD, 1740, ItemRarity.EPIC, PLAINS);
        registerBiomeFish("tetra", Items.COD, 21900, ItemRarity.LEGENDARY, PLAINS);
        registerBiomeFish("sawtooth", Items.TROPICAL_FISH, 44700, ItemRarity.LEGENDARY, PLAINS);
        registerBiomeFish("sea_serpent", Items.SALMON, 63100, ItemRarity.LEGENDARY, PLAINS);
        registerBiomeFish("zephyr", Items.PUFFERFISH, 80800, ItemRarity.LEGENDARY, PLAINS);
        registerBiomeFish("red_herring", Items.SALMON, 96500, ItemRarity.MYTHIC, PLAINS);
        registerBiomeFish("storm", Items.COD, 135000, ItemRarity.CHROMATIC, PLAINS);
        registerBiomeFish("kraken", Items.TROPICAL_FISH, 275000, ItemRarity.CELESTIAL, PLAINS);
    }
    private static void registerTropical() {
        registerBiomeFish("gar", Items.COD, 12, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("pike", Items.TROPICAL_FISH, 16, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("bream", Items.SALMON, 21, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("grouper", Items.COD, 27, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("magikarp", Items.TROPICAL_FISH, 33, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("minnow", Items.COD, 45, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("carp", Items.SALMON, 57, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("stalin", Items.COD, 80, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("m", Items.PUFFERFISH, 93, ItemRarity.COMMON, TROPICAL);
        registerBiomeFish("fusilier", Items.COD, 122, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("yellowtail", Items.SALMON, 156, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("lobster", Items.TROPICAL_FISH, 221, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("femfish", Items.SALMON, 289, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("yearfish", Items.PUFFERFISH, 365, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("furry", Items.TROPICAL_FISH, 420, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("fhyaian", Items.SALMON, 555, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("koi", Items.TROPICAL_FISH, 685, ItemRarity.UNCOMMON, TROPICAL);
        registerBiomeFish("stingray", Items.COD, 720, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("flying_fish", Items.COD, 777, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("karlfish", Items.SALMON, 1020, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("nickfish", Items.TROPICAL_FISH, 1212, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("angelfish", Items.PUFFERFISH, 1450, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("robofish", Items.SALMON, 1643, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("harp", Items.TROPICAL_FISH, 1917, ItemRarity.RARE, TROPICAL);
        registerBiomeFish("pandatail", Items.COD, 4510, ItemRarity.EPIC, TROPICAL);
        registerBiomeFish("man_o_war", Items.TROPICAL_FISH, 7550, ItemRarity.EPIC, TROPICAL);
        registerBiomeFish("marlin", Items.COD, 8990, ItemRarity.EPIC, TROPICAL);
        registerBiomeFish("wyvern", Items.PUFFERFISH, 10660, ItemRarity.EPIC, TROPICAL);
        registerBiomeFish("3y3", Items.PUFFERFISH, 12000, ItemRarity.EPIC, TROPICAL);
        registerBiomeFish("aleph", Items.COD, 16500, ItemRarity.LEGENDARY, TROPICAL);
        registerBiomeFish("tox", Items.TROPICAL_FISH, 32500, ItemRarity.LEGENDARY, TROPICAL);
        registerBiomeFish("kingfish", Items.COD, 45500, ItemRarity.LEGENDARY, TROPICAL);
        registerBiomeFish("peridot", Items.SALMON, 61500, ItemRarity.LEGENDARY, TROPICAL);
        registerBiomeFish("diastima", Items.COD, 95000, ItemRarity.MYTHIC, TROPICAL);
        registerBiomeFish("thalassa", Items.SALMON, 102500, ItemRarity.MYTHIC, TROPICAL);
        registerBiomeFish("asteri", Items.TROPICAL_FISH, 162500, ItemRarity.CHROMATIC, TROPICAL);
        registerBiomeFish("leviathan", Items.PUFFERFISH, 300000, ItemRarity.CELESTIAL, TROPICAL);
    }
    private static void registerDesert() {
        registerBiomeFish("sandy", Items.SALMON, 18, ItemRarity.COMMON, DESERT);
        registerBiomeFish("tadpole", Items.TROPICAL_FISH, 22, ItemRarity.COMMON, DESERT);
        registerBiomeFish("cactail", Items.SALMON, 28, ItemRarity.COMMON, DESERT);
        registerBiomeFish("scarab", Items.PUFFERFISH, 35, ItemRarity.COMMON, DESERT);
        registerBiomeFish("spotted_salmon", Items.COD, 41, ItemRarity.COMMON, DESERT);
        registerBiomeFish("pinfish", Items.SALMON, 48, ItemRarity.COMMON, DESERT);
        registerBiomeFish("scissortail", Items.COD, 56, ItemRarity.COMMON, DESERT);
        registerBiomeFish("alligator", Items.SALMON, 62, ItemRarity.COMMON, DESERT);
        registerBiomeFish("canetail", Items.PUFFERFISH, 71, ItemRarity.COMMON, DESERT);
        registerBiomeFish("miguelius", Items.TROPICAL_FISH, 72, ItemRarity.COMMON, DESERT);
        registerBiomeFish("wrasse", Items.PUFFERFISH, 76, ItemRarity.COMMON, DESERT);
        registerBiomeFish("miserium", Items.TROPICAL_FISH, 82, ItemRarity.COMMON, DESERT);
        registerBiomeFish("scorpion", Items.COD, 104, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("dolphin", Items.SALMON, 153, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("bombtail", Items.TROPICAL_FISH, 189, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("rustfin", Items.COD, 243, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("sheath", Items.SALMON, 298, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("sunbaked", Items.COD, 341, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("mackerel", Items.SALMON, 465, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("goby", Items.SALMON, 544, ItemRarity.UNCOMMON, DESERT);
        registerBiomeFish("arkfish", Items.PUFFERFISH, 610, ItemRarity.RARE, DESERT);
        registerBiomeFish("mossfish", Items.PUFFERFISH, 829, ItemRarity.RARE, DESERT);
        registerBiomeFish("samtail", Items.TROPICAL_FISH, 999, ItemRarity.RARE, DESERT);
        registerBiomeFish("c4", Items.TROPICAL_FISH, 1375, ItemRarity.RARE, DESERT);
        registerBiomeFish("sunfish", Items.TROPICAL_FISH, 1620, ItemRarity.RARE, DESERT);
        registerBiomeFish("fish_o_tron", Items.SALMON, 1809, ItemRarity.RARE, DESERT);
        registerBiomeFish("ambertail", Items.TROPICAL_FISH, 4560, ItemRarity.EPIC, DESERT);
        registerBiomeFish("starfish", Items.TROPICAL_FISH, 5580, ItemRarity.EPIC, DESERT);
        registerBiomeFish("pinkfog", Items.COD, 7980, ItemRarity.EPIC, DESERT);
        registerBiomeFish("dynafish", Items.COD, 9670, ItemRarity.EPIC, DESERT);
        registerBiomeFish("boreal", Items.PUFFERFISH, 11010, ItemRarity.EPIC, DESERT);
        registerBiomeFish("orca", Items.COD, 12130, ItemRarity.EPIC, DESERT);
        registerBiomeFish("krypt", Items.SALMON, 17200, ItemRarity.LEGENDARY, DESERT);
        registerBiomeFish("mirage", Items.COD, 24500, ItemRarity.LEGENDARY, DESERT);
        registerBiomeFish("stargaze", Items.TROPICAL_FISH, 43500, ItemRarity.LEGENDARY, DESERT);
        registerBiomeFish("reaper", Items.SALMON, 68500, ItemRarity.LEGENDARY, DESERT);
        registerBiomeFish("zeus", Items.PUFFERFISH, 92500, ItemRarity.MYTHIC, DESERT);
        registerBiomeFish("prismite", Items.PUFFERFISH, 155000, ItemRarity.CHROMATIC, DESERT);
        registerBiomeFish("megalodon", Items.SALMON, 262500, ItemRarity.CELESTIAL, DESERT);

    }
    private static void registerSnowy() {
        registerBiomeFish("eel", Items.SALMON, 15, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("icefish", Items.COD, 20, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("saury", Items.SALMON, 24, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("cusk", Items.COD, 28, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("haddock", Items.COD, 32, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("swoosh", Items.SALMON, 36, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("penguin", Items.SALMON, 42, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("albacore", Items.SALMON, 47, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("lungfish", Items.SALMON, 54, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("sardine", Items.SALMON, 58, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("frost_minnow", Items.SALMON, 64, ItemRarity.COMMON, SNOWY);
        registerBiomeFish("polar_bear", Items.SALMON, 90, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("viperfish", Items.COD, 176, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("bluetang", Items.COD, 256, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("idfish", Items.COD, 298, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("clam", Items.TROPICAL_FISH, 361, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("byronne", Items.TROPICAL_FISH, 419, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("gugutang", Items.TROPICAL_FISH, 505, ItemRarity.UNCOMMON, SNOWY);
        registerBiomeFish("sculk_infused_moonfish", Items.ECHO_SHARD, 706, ItemRarity.RARE, SNOWY);
        registerBiomeFish("electric_eel", Items.SALMON, 882, ItemRarity.RARE, SNOWY);
        registerBiomeFish("lionfish", Items.TROPICAL_FISH, 925, ItemRarity.RARE, SNOWY);
        registerBiomeFish("axolotl", Items.PUFFERFISH, 1180, ItemRarity.RARE, SNOWY);
        registerBiomeFish("glassfish", Items.PUFFERFISH, 1383, ItemRarity.RARE, SNOWY);
        registerBiomeFish("gallina", Items.PUFFERFISH, 1598, ItemRarity.RARE, SNOWY);
        registerBiomeFish("glace", Items.COD, 1808, ItemRarity.RARE, SNOWY);
        registerBiomeFish("rainbow_trout", Items.PUFFERFISH, 4110, ItemRarity.EPIC, SNOWY);
        registerBiomeFish("sabertooth", Items.SALMON, 5060, ItemRarity.EPIC, SNOWY);
        registerBiomeFish("manta_ray", Items.TROPICAL_FISH, 7280, ItemRarity.EPIC, SNOWY);
        registerBiomeFish("mako", Items.SALMON, 8650, ItemRarity.EPIC, SNOWY);
        registerBiomeFish("beluga", Items.COD, 10560, ItemRarity.EPIC, SNOWY);
        registerBiomeFish("berg", Items.COD, 13100, ItemRarity.EPIC, SNOWY);
        registerBiomeFish("painted_salmon", Items.COD, 18800, ItemRarity.LEGENDARY, SNOWY);
        registerBiomeFish("sculk_infused_chimera", Items.ECHO_SHARD, 34200, ItemRarity.LEGENDARY, SNOWY);
        registerBiomeFish("thresher", Items.SALMON, 51600, ItemRarity.LEGENDARY, SNOWY);
        registerBiomeFish("great_white_shark", Items.SALMON, 72300, ItemRarity.LEGENDARY, SNOWY);
        registerBiomeFish("poseidon", Items.COD, 82500, ItemRarity.MYTHIC, SNOWY);
        registerBiomeFish("hydra", Items.SALMON, 105000, ItemRarity.MYTHIC, SNOWY);
        registerBiomeFish("yeti", Items.SALMON, 145000, ItemRarity.CHROMATIC, SNOWY);
        registerBiomeFish("bahamut", Items.PUFFERFISH, 287500, ItemRarity.CELESTIAL, SNOWY);
    }
    private static void registerLava() {
        registerBiomeFish("mahi-mahi", Items.SALMON, 14, ItemRarity.COMMON, LAVA);
        registerBiomeFish("flamecrab", Items.COD, 18, ItemRarity.COMMON, LAVA);
        registerBiomeFish("lava_snail", Items.PUFFERFISH, 24, ItemRarity.COMMON, LAVA);
        registerBiomeFish("firefin", Items.PUFFERFISH, 33, ItemRarity.COMMON, LAVA);
        registerBiomeFish("obsidine", Items.SALMON, 49, ItemRarity.COMMON, LAVA);
        registerBiomeFish("strider", Items.SALMON, 53, ItemRarity.COMMON, LAVA);
        registerBiomeFish("magma_cube", Items.TROPICAL_FISH, 62, ItemRarity.COMMON, LAVA);
        registerBiomeFish("pigfish", Items.PUFFERFISH, 66, ItemRarity.COMMON, LAVA);
        registerBiomeFish("basalfin", Items.COD, 70, ItemRarity.COMMON, LAVA);
        registerBiomeFish("blacktang", Items.SALMON, 74, ItemRarity.COMMON, LAVA);
        registerBiomeFish("pyroleech", Items.PUFFERFISH, 76, ItemRarity.COMMON, LAVA);
        registerBiomeFish("bloodfin", Items.PUFFERFISH, 80, ItemRarity.COMMON, LAVA);
        registerBiomeFish("spiderfish", Items.SALMON, 120, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("grenadier", Items.COD, 152, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("magmatang", Items.PUFFERFISH, 214, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("forgefin", Items.PUFFERFISH, 271, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("scorchmel", Items.PUFFERFISH, 349, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("bruisette", Items.SALMON, 464, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("lacera", Items.TROPICAL_FISH, 512, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("sinis", Items.SALMON, 576, ItemRarity.UNCOMMON, LAVA);
        registerBiomeFish("ichorfish", Items.PUFFERFISH, 620, ItemRarity.RARE, LAVA);
        registerBiomeFish("flarefin", Items.PUFFERFISH, 892, ItemRarity.RARE, LAVA);
        registerBiomeFish("wartfin", Items.SALMON, 1014, ItemRarity.RARE, LAVA);
        registerBiomeFish("magnalav", Items.PUFFERFISH, 1285, ItemRarity.RARE, LAVA);
        registerBiomeFish("volcan", Items.COD, 1483, ItemRarity.RARE, LAVA);
        registerBiomeFish("ghoul", Items.TROPICAL_FISH, 1671, ItemRarity.RARE, LAVA);
        registerBiomeFish("blazier", Items.PUFFERFISH, 2041, ItemRarity.RARE, LAVA);
        registerBiomeFish("hemopsari", Items.PUFFERFISH, 4570, ItemRarity.EPIC, LAVA);
        registerBiomeFish("pyroxene", Items.COD, 5740, ItemRarity.EPIC, LAVA);
        registerBiomeFish("tephra", Items.SALMON, 7530, ItemRarity.EPIC, LAVA);
        registerBiomeFish("broken_soul", Items.TROPICAL_FISH, 9660, ItemRarity.EPIC, LAVA);
        registerBiomeFish("finferno", Items.PUFFERFISH, 10280, ItemRarity.EPIC, LAVA);
        registerBiomeFish("purgaton", Items.SALMON, 12200, ItemRarity.EPIC, LAVA);
        registerBiomeFish("painted_tropical_fish", Items.PUFFERFISH, 23200, ItemRarity.LEGENDARY, LAVA);
        registerBiomeFish("illumi", Items.PUFFERFISH, 44100, ItemRarity.LEGENDARY, LAVA);
        registerBiomeFish("molt", Items.PUFFERFISH, 65800, ItemRarity.LEGENDARY, LAVA);
        registerBiomeFish("kuudra", Items.COD, 72400, ItemRarity.LEGENDARY, LAVA);
        registerBiomeFish("hades", Items.COD, 87500, ItemRarity.MYTHIC, LAVA);
        registerBiomeFish("xenomorph", Items.SALMON, 97500, ItemRarity.MYTHIC, LAVA);
        registerBiomeFish("beelzebub", Items.COD, 170000, ItemRarity.CHROMATIC, LAVA);
        registerBiomeFish("cthulhu", Items.COD, 312500, ItemRarity.CELESTIAL, LAVA);

    }
    private static void registerDeep() {
        registerBiomeFish("rockfish", Items.SALMON, 17, ItemRarity.COMMON, DEEP);
        registerBiomeFish("stonefish", Items.COD, 19, ItemRarity.COMMON, DEEP);
        registerBiomeFish("halibut", Items.SALMON, 22, ItemRarity.COMMON, DEEP);
        registerBiomeFish("andera", Items.SALMON, 25, ItemRarity.COMMON, DEEP);
        registerBiomeFish("coalcarp", Items.COD, 30, ItemRarity.COMMON, DEEP);
        registerBiomeFish("copperbit", Items.COD, 37, ItemRarity.COMMON, DEEP);
        registerBiomeFish("granisie", Items.COD, 44, ItemRarity.COMMON, DEEP);
        registerBiomeFish("dioritan", Items.SALMON, 50, ItemRarity.COMMON, DEEP);
        registerBiomeFish("silverfish", Items.SALMON, 53, ItemRarity.COMMON, DEEP);
        registerBiomeFish("claylen", Items.SALMON, 59, ItemRarity.COMMON, DEEP);
        registerBiomeFish("gravene", Items.SALMON, 67, ItemRarity.COMMON, DEEP);
        registerBiomeFish("lanternfish", Items.TROPICAL_FISH, 74, ItemRarity.COMMON, DEEP);
        registerBiomeFish("glowlight_danio", Items.TROPICAL_FISH, 88, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("quartzray", Items.TROPICAL_FISH, 142, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("sardiron", Items.SALMON, 202, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("bronslate", Items.PUFFERFISH, 276, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("feldspear", Items.PUFFERFISH, 369, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("vanesse", Items.PUFFERFISH, 440, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("wilsine", Items.PUFFERFISH, 494, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("tuffffin", Items.TROPICAL_FISH, 554, ItemRarity.UNCOMMON, DEEP);
        registerBiomeFish("moai", Items.SALMON, 603, ItemRarity.RARE, DEEP);
        registerBiomeFish("lapine", Items.PUFFERFISH, 793, ItemRarity.RARE, DEEP);
        registerBiomeFish("redstil", Items.PUFFERFISH, 975, ItemRarity.RARE, DEEP);
        registerBiomeFish("prismaray", Items.COD, 1207, ItemRarity.RARE, DEEP);
        registerBiomeFish("ametang", Items.TROPICAL_FISH, 1436, ItemRarity.RARE, DEEP);
        registerBiomeFish("photopectoralis", Items.TROPICAL_FISH, 1732, ItemRarity.RARE, DEEP);
        registerBiomeFish("cryster", Items.TROPICAL_FISH, 1941, ItemRarity.RARE, DEEP);
        registerBiomeFish("anglerfish", Items.TROPICAL_FISH, 5620, ItemRarity.EPIC, DEEP);
        registerBiomeFish("frogold", Items.TROPICAL_FISH, 6860, ItemRarity.EPIC, DEEP);
        registerBiomeFish("emerafin", Items.SALMON, 8300, ItemRarity.EPIC, DEEP);
        registerBiomeFish("bedroam", Items.SALMON, 10250, ItemRarity.EPIC, DEEP);
        registerBiomeFish("minerafin", Items.PUFFERFISH, 11430, ItemRarity.EPIC, DEEP);
        registerBiomeFish("sahur", Items.PUFFERFISH, 12750, ItemRarity.EPIC, DEEP);
        registerBiomeFish("sierra", Items.COD, 25800, ItemRarity.LEGENDARY, DEEP);
        registerBiomeFish("diamondfish", Items.SALMON, 39800, ItemRarity.LEGENDARY, DEEP);
        registerBiomeFish("cyclops", Items.SALMON, 62400, ItemRarity.LEGENDARY, DEEP);
        registerBiomeFish("rascal", Items.COD, 83000, ItemRarity.LEGENDARY, DEEP);
        registerBiomeFish("gaia", Items.PUFFERFISH, 94000, ItemRarity.MYTHIC, DEEP);
        registerBiomeFish("scylla", Items.PUFFERFISH, 106000, ItemRarity.MYTHIC, DEEP);
        registerBiomeFish("sentinel", Items.SALMON, 175000, ItemRarity.CHROMATIC, DEEP);
        registerBiomeFish("bakunawa", Items.PUFFERFISH, 325000, ItemRarity.CELESTIAL, DEEP);
    }
    private static void registerEnd() {
        registerBiomeFish("tilapia", Items.STAINED_GLASS_PANE.lightGray(), 17, ItemRarity.COMMON, END);
        registerBiomeFish("endermite", Items.STAINED_GLASS_PANE.lightGray(), 19, ItemRarity.COMMON, END);
        registerBiomeFish("voidling", Items.STAINED_GLASS_PANE.lightGray(), 23, ItemRarity.COMMON, END);
        registerBiomeFish("oxyene", Items.STAINED_GLASS_PANE.lightGray(), 29, ItemRarity.COMMON, END);
        registerBiomeFish("endon", Items.STAINED_GLASS_PANE.lightGray(), 37, ItemRarity.COMMON, END);
        registerBiomeFish("wastefin", Items.STAINED_GLASS_PANE.lightGray(), 41, ItemRarity.COMMON, END);
        registerBiomeFish("barrere", Items.STAINED_GLASS_PANE.lightGray(), 46, ItemRarity.COMMON, END);
        registerBiomeFish("limbo", Items.STAINED_GLASS_PANE.lightGray(), 51, ItemRarity.COMMON, END);
        registerBiomeFish("sans", Items.STAINED_GLASS_PANE.lightGray(), 57, ItemRarity.COMMON, END);
        registerBiomeFish("piscis", Items.STAINED_GLASS_PANE.lightGray(), 63, ItemRarity.COMMON, END);
        registerBiomeFish("inane", Items.STAINED_GLASS_PANE.lightGray(), 69, ItemRarity.COMMON, END);
        registerBiomeFish("terminus", Items.STAINED_GLASS_PANE.lightGray(), 74, ItemRarity.COMMON, END);
        registerBiomeFish("shulke", Items.STAINED_GLASS_PANE.lightGray(), 91, ItemRarity.UNCOMMON, END);
        registerBiomeFish("illusionelle", Items.STAINED_GLASS_PANE.lightGray(), 113, ItemRarity.UNCOMMON, END);
        registerBiomeFish("vacutail", Items.STAINED_GLASS_PANE.lightGray(), 190, ItemRarity.UNCOMMON, END);
        registerBiomeFish("lepisma", Items.STAINED_GLASS_PANE.lightGray(), 341, ItemRarity.UNCOMMON, END);
        registerBiomeFish("cosmo", Items.STAINED_GLASS_PANE.lightGray(), 465, ItemRarity.UNCOMMON, END);
        registerBiomeFish("senza", Items.STAINED_GLASS_PANE.lightGray(), 527, ItemRarity.UNCOMMON, END);
        registerBiomeFish("nihil", Items.STAINED_GLASS_PANE.lightGray(), 591, ItemRarity.UNCOMMON, END);
        registerBiomeFish("vocivus", Items.STAINED_GLASS_PANE.lightGray(), 645, ItemRarity.UNCOMMON, END);
        registerBiomeFish("moonfish", Items.STAINED_GLASS_PANE.lightGray(), 813, ItemRarity.RARE, END);
        registerBiomeFish("levifin", Items.STAINED_GLASS_PANE.lightGray(), 1070, ItemRarity.RARE, END);
        registerBiomeFish("petrichor", Items.STAINED_GLASS_PANE.lightGray(), 1310, ItemRarity.RARE, END);
        registerBiomeFish("choru", Items.STAINED_GLASS_PANE.lightGray(), 1682, ItemRarity.RARE, END);
        registerBiomeFish("aeon", Items.STAINED_GLASS_PANE.lightGray(), 1904, ItemRarity.RARE, END);
        registerBiomeFish("planetario", Items.STAINED_GLASS_PANE.lightGray(), 2133, ItemRarity.RARE, END);
        registerBiomeFish("divane", Items.STAINED_GLASS_PANE.lightGray(), 2452, ItemRarity.RARE, END);
        registerBiomeFish("dragonfish", Items.STAINED_GLASS_PANE.lightGray(), 4444, ItemRarity.EPIC, END);
        registerBiomeFish("abyssa", Items.STAINED_GLASS_PANE.lightGray(), 5555, ItemRarity.EPIC, END);
        registerBiomeFish("hollowan", Items.STAINED_GLASS_PANE.lightGray(), 7777, ItemRarity.EPIC, END);
        registerBiomeFish("4o4", Items.STAINED_GLASS_PANE.lightGray(), 9999, ItemRarity.EPIC, END);
        registerBiomeFish("corruptin", Items.STAINED_GLASS_PANE.lightGray(), 11111, ItemRarity.EPIC, END);
        registerBiomeFish("andromeda", Items.STAINED_GLASS_PANE.lightGray(), 12321, ItemRarity.EPIC, END);
        registerBiomeFish("painted_pufferfish", Items.STAINED_GLASS_PANE.lightGray(), 22222, ItemRarity.LEGENDARY, END);
        registerBiomeFish("empty", Items.STAINED_GLASS_PANE.lightGray(), 33333, ItemRarity.LEGENDARY, END);
        registerBiomeFish("hex0101coda", Items.STAINED_GLASS_PANE.lightGray(), 50000, ItemRarity.LEGENDARY, END);
        registerBiomeFish("infinity", Items.STAINED_GLASS_PANE.lightGray(), 77777, ItemRarity.LEGENDARY, END);
        registerBiomeFish("undefined", Items.STAINED_GLASS_PANE.lightGray(), 99999, ItemRarity.MYTHIC, END);
        registerBiomeFish("null", Items.STAINED_GLASS_PANE.lightGray(), 111111, ItemRarity.MYTHIC, END);
        registerBiomeFish("galax", Items.STAINED_GLASS_PANE.lightGray(), 171717, ItemRarity.CHROMATIC, END);
        registerBiomeFish("ningen", Items.STAINED_GLASS_PANE.lightGray(), 337500, ItemRarity.CELESTIAL, END);
    }
    private static void registerSky() {
        registerBiomeFish("pollock", Items.SALMON, 24, ItemRarity.COMMON, SKY);
        registerBiomeFish("goldfish", Items.COD, 30, ItemRarity.COMMON, SKY);
        registerBiomeFish("perch", Items.SALMON, 38, ItemRarity.COMMON, SKY);
        registerBiomeFish("turtle", Items.TROPICAL_FISH, 46, ItemRarity.COMMON, SKY);
        registerBiomeFish("wave", Items.PUFFERFISH, 51, ItemRarity.COMMON, SKY);
        registerBiomeFish("nestie", Items.TROPICAL_FISH, 58, ItemRarity.COMMON, SKY);
        registerBiomeFish("mesher", Items.SALMON, 71, ItemRarity.COMMON, SKY);
        registerBiomeFish("avia", Items.SALMON, 76, ItemRarity.COMMON, SKY);
        registerBiomeFish("starlie", Items.TROPICAL_FISH, 83, ItemRarity.COMMON, SKY);
        registerBiomeFish("aerofin", Items.COD, 91, ItemRarity.COMMON, SKY);
        registerBiomeFish("cawl", Items.COD, 99, ItemRarity.COMMON, SKY);
        registerBiomeFish("vultan", Items.COD, 101, ItemRarity.COMMON, SKY);
        registerBiomeFish("cloudfish", Items.SALMON, 156, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("seahorse", Items.PUFFERFISH, 236, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("swanzie", Items.SALMON, 303, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("leape", Items.SALMON, 379, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("flamingo", Items.PUFFERFISH, 481, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("windle", Items.SALMON, 593, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("cutskydiver", Items.TROPICAL_FISH, 675, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("mistie", Items.SALMON, 777, ItemRarity.UNCOMMON, SKY);
        registerBiomeFish("sailfish", Items.COD, 908, ItemRarity.RARE, SKY);
        registerBiomeFish("vaporfin", Items.SALMON, 1142, ItemRarity.RARE, SKY);
        registerBiomeFish("hazy", Items.SALMON, 1303, ItemRarity.RARE, SKY);
        registerBiomeFish("nitron", Items.PUFFERFISH, 1622, ItemRarity.RARE, SKY);
        registerBiomeFish("aria", Items.SALMON, 1965, ItemRarity.RARE, SKY);
        registerBiomeFish("john", Items.COD, 2340, ItemRarity.RARE, SKY);
        registerBiomeFish("caelum", Items.SALMON, 2706, ItemRarity.RARE, SKY);
        registerBiomeFish("altus", Items.SALMON, 5160, ItemRarity.EPIC, SKY);
        registerBiomeFish("aura", Items.SALMON, 9340, ItemRarity.EPIC, SKY);
        registerBiomeFish("soul", Items.PUFFERFISH, 12350, ItemRarity.EPIC, SKY);
        registerBiomeFish("nebulo", Items.SALMON, 14040, ItemRarity.EPIC, SKY);
        registerBiomeFish("holine", Items.TROPICAL_FISH, 16800, ItemRarity.EPIC, SKY);
        registerBiomeFish("spatia", Items.SALMON, 19200, ItemRarity.EPIC, SKY);
        registerBiomeFish("aerie", Items.SALMON, 31300, ItemRarity.LEGENDARY, SKY);
        registerBiomeFish("unicorn", Items.TROPICAL_FISH, 54600, ItemRarity.LEGENDARY, SKY);
        registerBiomeFish("aether", Items.SALMON, 79300, ItemRarity.LEGENDARY, SKY);
        registerBiomeFish("arceus", Items.TROPICAL_FISH, 92500, ItemRarity.LEGENDARY, SKY);
        registerBiomeFish("elysia", Items.TROPICAL_FISH, 110000, ItemRarity.MYTHIC, SKY);
        registerBiomeFish("sylph", Items.PUFFERFISH, 125000, ItemRarity.MYTHIC, SKY);
        registerBiomeFish("icarus", Items.SALMON, 180000, ItemRarity.CHROMATIC, SKY);
        registerBiomeFish("lusca", Items.TROPICAL_FISH, 350000, ItemRarity.CELESTIAL, SKY);
    }
}
