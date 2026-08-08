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
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class FishingLoot {
    public static void rewardFish(ServerPlayer player, FishingHook hook, RodTiers tier) {
        RandomSource random = minecraftServer.overworld().getRandom();
        FishItem caughtFish = getRandomFishForTier(tier);
        int maxStars = Math.min(3, tier.ordinal() + 1);
        int quality = random.nextInt(maxStars + 1);
        List<FishModifier> traits = new ArrayList<>();
        double traitChance = 0.2 * tier.getCatchLuckBonus();

        List<FishModifier> allTraits = new ArrayList<>(Arrays.stream(FishModifier.values()).toList());
        while (!allTraits.isEmpty() && random.nextDouble() < traitChance) {
            int index = random.nextInt(allTraits.size());
            FishModifier selected = allTraits.remove(index);
            allTraits.add(selected);

            traitChance *= 0.5;
        }

        ItemStack fishStack = caughtFish.createFishInstance(quality, traits);
        if (!player.getInventory().add(fishStack)) player.drop(fishStack, false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.2f);
        Component fishName = fishStack.get(DataComponents.ITEM_NAME);
        MessageUtils.sendSuccessMessage(player, String.format("You caught a %s!", fishName != null ? fishName.getString() : caughtFish.getFishName()));
    }

    private static final double[][] RARITY_WEIGHTS_PER_TIER = {
            {70, 25, 4.5, .344, .125, .025, .005, .001},
            {55, 30, 12, 2.7, .225, .75, 0, 0},
            {40, 32, 18, 8, 1.9, .1, 0, 0},
            {25, 30, 25, 12, 6, 1.8, .2, 0},
            {15, 22, 28, 18, 11, 4.5, 1.4, .1},
            {8, 14, 24, 22, 16, 10, 5, 1},
            {3, 7, 15, 25, 22, 15, 9.5, 3.5}
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
}