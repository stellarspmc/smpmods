package fun.spmc.smpmod.treasure;

import fun.spmc.smpmod.misc.ItemRarity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static fun.spmc.smpmod.treasure.TreasureEvents.getBaseCommonChance;

public class TreasureHelper {
    public static boolean rigTreasures = false;

    private static final float THRESHOLD_CELESTIAL = .0001f;                      // .0001%
    private static final float THRESHOLD_CHROMATIC = THRESHOLD_CELESTIAL + .0005f;// .0005%
    private static final float THRESHOLD_MYTHIC = THRESHOLD_CHROMATIC + .0024f;   // .0024%
    private static final float THRESHOLD_LEGENDARY = THRESHOLD_MYTHIC + .007f;    // .007%
    private static final float THRESHOLD_EPIC = THRESHOLD_LEGENDARY + .03f;       // .03%
    private static final float THRESHOLD_RARE = THRESHOLD_EPIC + .12f;            // .12%
    private static final float THRESHOLD_UNCOMMON = THRESHOLD_RARE + .28f;        // .28%
    private static final float THRESHOLD_COMMON = THRESHOLD_UNCOMMON + .56f;      // .56%

    public static ItemRarity rollTreasureRarity(BlockState state, double fatigueMultiplier, RandomSource random, ResourceKey<Level> dimension) {
        float commonChance = (float) (getBaseCommonChance(state, dimension) * fatigueMultiplier);
        if (commonChance <= 0) return null;
        float val = (random.nextFloat() * 100f) / commonChance;
        if (val < THRESHOLD_MYTHIC) return ItemRarity.MYTHIC;
        if (val < THRESHOLD_LEGENDARY) return adjustRarity(ItemRarity.LEGENDARY);
        if (val < THRESHOLD_EPIC) return adjustRarity(ItemRarity.EPIC);
        if (val < THRESHOLD_RARE) return adjustRarity(ItemRarity.RARE);
        if (val < THRESHOLD_COMMON) return adjustRarity(ItemRarity.COMMON);
        return null;
    }

    private static ItemRarity adjustRarity(ItemRarity rarity) {
        if (!rigTreasures) return rarity;
        return switch (rarity) {
            case LEGENDARY -> ItemRarity.MYTHIC;
            case EPIC -> ItemRarity.LEGENDARY;
            case RARE -> ItemRarity.EPIC;
            case COMMON -> ItemRarity.RARE;
            default -> rarity;
        };
    }

    public static ItemRarity rollRarity(float roll) { // keep in mind, this is all 1%
        if (roll < THRESHOLD_CELESTIAL) return ItemRarity.CELESTIAL;
        if (roll < THRESHOLD_CHROMATIC) return ItemRarity.CHROMATIC;
        if (roll < THRESHOLD_MYTHIC)    return ItemRarity.MYTHIC;
        if (roll < THRESHOLD_LEGENDARY) return ItemRarity.LEGENDARY;
        if (roll < THRESHOLD_EPIC)      return ItemRarity.EPIC;
        if (roll < THRESHOLD_RARE)      return ItemRarity.RARE;
        if (roll < THRESHOLD_UNCOMMON)  return ItemRarity.UNCOMMON;
        return ItemRarity.COMMON;
    }
}
