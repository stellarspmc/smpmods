package ml.spmc.smpmod.utils.treasure;

import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;

public enum TreasureRarities {
    COMMON(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL), 5, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_INGOT), 5, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_NUGGET), 5, 1)
    ),
    RARE(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL), 15, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_INGOT), 15, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_INGOT), 5, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.DIAMOND), 2, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 5, 1)
    ),
    EPIC(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL_BLOCK), 2, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 1, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_INGOT), 8, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.DIAMOND), 9, 2),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 15, 5)
    ),
    LEGENDARY(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL_BLOCK), 17, 3),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 5, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_BLOCK), 7, 7),
            new ItemStackWithMaxMin(new ItemStack(Items.DIAMOND_BLOCK), 4, 2),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 35, 15)

    ),
    ULTIMATE(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL_BLOCK), 128, 64),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 16, 16),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_INGOT), 19, 19),
            new ItemStackWithMaxMin(new ItemStack(Items.NETHERITE_BLOCK), 1, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 175, 35)
    );

    ItemStackWithMaxMin[] stacks;

    TreasureRarities(ItemStackWithMaxMin... items) {
        stacks = items;
    }

    public ArrayList<ItemStack> rollStack(TreasureRarities rarity, PlayerEntity player) {
        ArrayList<ItemStack> rolled = new ArrayList<>();
        if (rarity == ULTIMATE) return ItemStackWithMaxMin.convert(ULTIMATE.stacks);
        else {
            for (ItemStackWithMaxMin i : rarity.stacks) {
                if (UtilClass.probabilityCalc(50, player)) {
                    ItemStack item = i.getStack();
                    if (i.getMax() == i.getMin()) rolled.add(UtilClass.getAndSetCount(item, i.getMin()));
                    else {
                        int len = i.getMax() - i.getMin() + 1;
                        double pie = player.getRandom().nextDouble();

                        for (int b = 1; b==len; b++) {
                            double upper = Math.pow(2, (-b)+1);
                            double lower = Math.pow(2, -b);
                            if ((upper <= pie) && (pie > lower)) {
                                rolled.add(UtilClass.getAndSetCount(item, b));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return rolled;
    }
}
