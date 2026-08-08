package fun.spmc.smpmod.fishing.mechanic;

import fun.spmc.smpmod.SMPItems;
import fun.spmc.smpmod.fishing.FishModifier;
import fun.spmc.smpmod.fishing.FishRarity;
import fun.spmc.smpmod.fishing.fish.FishItem;
import fun.spmc.smpmod.fishing.rod.RodTiers;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class FishingLoot {
    public static void rewardFish(ServerPlayer player, RodTiers tier) {
        RandomSource random = minecraftServer.overworld().getRandom();
        FishItem caughtFish = getRandomFishForTier(tier);
        Map<FishModifier, Integer> modMap = new HashMap<>();
        double traitChance = 0.2 * tier.getCatchLuckBonus();

        List<FishModifier> mods = new ArrayList<>(Arrays.stream(FishModifier.values()).toList());
        while (!mods.isEmpty() && random.nextDouble() < traitChance) {
            int index = random.nextInt(mods.size());
            modMap.put(mods.remove(index), random.nextInt(5) + 1);
            traitChance *= .75 * tier.getCatchLuckBonus() / 1.8;
        }

        ItemStack fishStack = caughtFish.createFishInstance(rollStarQuality(random, tier.getCatchLuckBonus()), modMap);
        if (!player.getInventory().add(fishStack)) player.drop(fishStack, false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.2f);
        Component fishName = fishStack.get(DataComponents.ITEM_NAME);
        MessageUtils.sendSuccessMessage(player, String.format("You caught a %s!", fishName != null ? fishName.getString() : caughtFish.getFishName()));
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
        FishRarity selectedRarity = FishRarity.COMMON;

        FishRarity[] rarities = FishRarity.values();
        for (int i = 0; i < weights.length; i++) {
            current += weights[i];
            if (roll <= current) {
                selectedRarity = rarities[i];
                break;
            }
        }

        FishRarity finalRarity = selectedRarity;
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

        // Adjust weights based on luck bonus
        for (int star = 0; star < BASE_STAR_WEIGHTS.length; star++) {
            // Weight * (luckBonus ^ star)
            double weight = BASE_STAR_WEIGHTS[star] * Math.pow(luckBonus, star);
            adjustedWeights[star] = weight;
            totalWeight += weight;
        }

        // Roll weighted random value
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (int star = 0; star < adjustedWeights.length; star++) {
            cumulative += adjustedWeights[star];
            if (roll < cumulative) {
                return star;
            }
        }

        return 0; // Fallback
    }
}