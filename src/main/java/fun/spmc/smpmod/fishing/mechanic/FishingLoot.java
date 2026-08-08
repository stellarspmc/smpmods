package fun.spmc.smpmod.fishing.mechanic;

import fun.spmc.smpmod.SMPItems;
import fun.spmc.smpmod.misc.ItemModifier;
import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.fishing.fish.FishItem;
import fun.spmc.smpmod.fishing.rod.RodTiers;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fun.spmc.smpmod.SMPMod.messageChannel;
import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class FishingLoot {
    private static final Map<ResourceKey<Biome>, String> BIOME_MAP = new ConcurrentHashMap<>();

    public static void rewardFish(ServerPlayer player, RodTiers tier, int streak) {
        RandomSource random = minecraftServer.overworld().getRandom();
        FishItem caughtFish = getRandomFishForTier(tier);
        Map<ItemModifier, Integer> modMap = new HashMap<>();
        double traitChance = Math.max(.5, (((double) (tier.ordinal() + 1) / RodTiers.values().length) * streak) * .2 * tier.getCatchLuckBonus());

        List<ItemModifier> mods = new ArrayList<>(Arrays.stream(ItemModifier.values()).toList());
        while (!mods.isEmpty() && random.nextDouble() < traitChance) {
            int index = random.nextInt(mods.size());
            modMap.put(mods.remove(index), random.nextInt(5) + 1);
            traitChance *= Math.max(.4, .2 * tier.getCatchLuckBonus() / 1.8);
        }

        ItemStack fishStack = caughtFish.createFishInstance(rollStarQuality(random, tier.getCatchLuckBonus()), modMap);
        if (!player.getInventory().add(fishStack)) player.drop(fishStack, false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1, 1.2f);
        player.sendSystemMessage(Component.literal("You caught a ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(caughtFish.getFishName()).withStyle(caughtFish.getRarity().getColor()))
                .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
        if (caughtFish.getRarity() == ItemRarity.CHROMATIC) messageChannel.sendMessage(String.format("%s got a **CHROMATIC** %s.", MarkdownSanitizer.escape(player.getScoreboardName()), caughtFish.getFishName())).queue();
        else if (caughtFish.getRarity() == ItemRarity.CELESTIAL) messageChannel.sendMessage(String.format("%s got a **CELESTIAL** %s.", MarkdownSanitizer.escape(player.getScoreboardName()), caughtFish.getFishName())).queue();
    }

    private static final double[][] RARITY_WEIGHTS_PER_TIER = {
            { 75, 20, 4.2, .7, .09, .008, .0015, .0005 },
            { 62, 27, 8.5, 2, .4, .08, .018, .002 },
            { 48, 30, 15, 5.5, 1.2, .25, .04, .01 },
            { 35, 32, 20, 9, 3.0, .8, .15, .05 },
            { 24, 30, 26, 13, 5.5, 1.1, .3, .1 },
            { 14, 24, 30, 18, 9.0, 3.5, 1.1, .4 },
            { 8, 17, 32, 22, 12, 5.5, 2.5, 1 }
    };

    private static FishItem getRandomFishForTier(RodTiers tier) {
        List<Item> pool = SMPItems.FISH;
        if (pool.isEmpty()) throw new IllegalStateException("Fish pool is empty!");
        double[] weights = RARITY_WEIGHTS_PER_TIER[tier.ordinal()];
        double roll = minecraftServer.overworld().getRandom().nextDouble() * 100;
        double current = 0;
        ItemRarity selectedRarity = ItemRarity.COMMON;

        ItemRarity[] rarities = ItemRarity.values();
        for (int i = 0; i < weights.length; i++) {
            current += weights[i];
            if (roll <= current) {
                selectedRarity = rarities[i];
                break;
            }
        }

        ItemRarity finalRarity = selectedRarity;
        List<FishItem> matchingFish = SMPItems.FISH.stream()
                .filter(item -> item instanceof FishItem fish && fish.getRarity() == finalRarity)
                .map(item -> (FishItem) item)
                .toList();

        if (matchingFish.isEmpty()) return (FishItem) SMPItems.FISH.getFirst();

        double exponent = -1 + (tier.ordinal() * .2);

        double tierTotalWeight = 0;
        double[] fishWeights = new double[matchingFish.size()];

        for (int i = 0; i < matchingFish.size(); i++) {
            double weight = Math.pow(matchingFish.get(i).getBasePrice(), exponent);
            fishWeights[i] = weight;
            tierTotalWeight += weight;
        }

        double fishRoll = minecraftServer.overworld().getRandom().nextDouble() * tierTotalWeight;
        double fishWeight = 0;

        for (int i = 0; i < matchingFish.size(); i++) {
            fishWeight += fishWeights[i];
            if (fishRoll <= fishWeight) return matchingFish.get(i);
        }

        return matchingFish.getFirst();
    }

    private static final double[] BASE_STAR_WEIGHTS = { 1000.0, 600.0, 300.0, 120.0, 35.0, 6.0 };

    private static int rollStarQuality(RandomSource random, float luckBonus) {
        double[] adjustedWeights = new double[BASE_STAR_WEIGHTS.length];
        double totalWeight = 0;

        for (int star = 0; star < BASE_STAR_WEIGHTS.length; star++) {
            double weight = BASE_STAR_WEIGHTS[star] * Math.pow(luckBonus, star);
            adjustedWeights[star] = weight;
            totalWeight += weight;
        }

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (int star = 0; star < adjustedWeights.length; star++) {
            cumulative += adjustedWeights[star];
            if (roll < cumulative) return star;
        }

        return 0;
    }

    public static String getBiome(ResourceKey<Biome> biomeKey) {
        return BIOME_MAP.computeIfAbsent(biomeKey, key -> {
            String path = key.identifier().getPath();

            if (path.contains("badlands")) return "badlands";
            if (path.contains("desert")) return "desert";
            if (path.contains("dripstone")) return "dripstone";
            if (path.contains("dark_forest")) return "dark_forest";
            if (path.contains("deep_dark")) return "deep_dark";
            if (path.contains("lush_caves")) return "lush_caves";
            if (path.contains("mushroom")) return "mushroom";
            if (path.contains("swamp")) return "swamp";
            if (path.contains("jungle")) return "jungle";
            if (path.contains("taiga")) return "taiga";
            if (path.contains("savanna")) return "savanna";
            if (path.contains("ocean") || path.contains("beach")) return "ocean";

            if (path.contains("flower") || path.contains("meadow") || path.contains("cherry")) return "flower";
            if (path.contains("ice") || path.contains("frozen") || path.contains("snow")) return "ice";
            if (path.contains("peaks") || path.contains("slopes") || path.contains("stony")) return "mountain";
            if (path.contains("windswept")) return "wind";

            if (path.contains("basalt_deltas")) return "basalt";
            if (path.contains("crimson_forest")) return "crimson";
            if (path.contains("warped_forest")) return "warped";
            if (path.contains("soul_sand_valley")) return "soul_valley";
            if (path.contains("nether_wastes")) return "nether";

            if (key.identifier().getNamespace().equals("minecraft") && path.contains("end")) return "end";

            return "default";
        });
    }
}