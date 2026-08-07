package fun.spmc.smpmod.fishing.mechanic;

import fun.spmc.smpmod.SMPItems;
import fun.spmc.smpmod.fishing.FishModifier;
import fun.spmc.smpmod.fishing.fish.FishItem;
import fun.spmc.smpmod.fishing.rod.RodTiers;
import net.minecraft.ChatFormatting;
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
import java.util.List;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class FishingLoot {
    public static void rewardFish(ServerPlayer player, FishingHook hook, RodTiers tier) {
        RandomSource random = minecraftServer.overworld().getRandom();
        FishItem caughtFish = getRandomFishForTier(tier);
        int maxStars = Math.min(3, tier.ordinal() + 1);
        int quality = random.nextInt(maxStars + 1);
        List<FishModifier> traits = new ArrayList<>();
        double traitChance = 0.10 * tier.getCatchLuckBonus();

        if (random.nextDouble() < traitChance) {
            FishModifier[] allTraits = FishModifier.values();
            traits.add(allTraits[random.nextInt(allTraits.length)]);
        }

        ItemStack fishStack = caughtFish.createFishInstance(quality, traits);
        if (!player.getInventory().add(fishStack)) player.drop(fishStack, false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.2f);
        Component fishName = fishStack.get(DataComponents.ITEM_NAME);
        player.sendSystemMessage(
                Component.literal(" You caught a ").withStyle(ChatFormatting.GREEN)
                        .append(fishName != null ? fishName : Component.literal(caughtFish.getFishName()))
                        .append("!")
        );
    }

    private static FishItem getRandomFishForTier(RodTiers tier) {
        List<Item> pool = SMPItems.FISH;
        if (pool.isEmpty()) throw new IllegalStateException("Code not working, report to admin.");
        return (FishItem) pool.get(minecraftServer.overworld().getRandom().nextInt(pool.size()));
    }
}