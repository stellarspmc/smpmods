package fun.spmc.smpmod.fishing.mechanic;

import fun.spmc.smpmod.fishing.BiomeCategory;
import fun.spmc.smpmod.registry.PolymerFishes;
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
        FishItem caughtFish = getRandomFishForTier(player, tier);
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

    private static FishItem getRandomFishForTier(ServerPlayer player, RodTiers tier) {
        BiomeCategory category = BiomeCategory.getCategory(player);
        List<Item> pool = PolymerFishes.FISH;
        if (pool.isEmpty()) throw new IllegalStateException("Fish pool is empty!");
        double[] weights = tier.getRates();
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
        List<FishItem> matchingFish = PolymerFishes.FISH.stream()
                .filter(item -> item instanceof FishItem fish && fish.getRarity() == finalRarity)
                .map(item -> (FishItem) item)
                .toList();

        if (matchingFish.isEmpty()) return (FishItem) PolymerFishes.FISH.getFirst();

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

    private static final double[] BASE_STAR_WEIGHTS = { 1000.0, 600.0, 300.0, 120.0, 35, 6 };

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
}