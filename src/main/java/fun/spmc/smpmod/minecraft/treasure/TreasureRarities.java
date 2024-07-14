package fun.spmc.smpmod.minecraft.treasure;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;

public enum TreasureRarities {
    COMMON(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL), 53, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_INGOT), 53, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_NUGGET), 53, 1)
    ),
    RARE(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL), 153, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_INGOT), 153, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_INGOT), 53, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.DIAMOND), 25, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 5, 1)
    ),
    EPIC(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL_BLOCK), 233, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 133, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 83, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.DIAMOND), 435, 2),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 15, 5)
    ),
    LEGENDARY(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL_BLOCK), 173, 3),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 533, 5),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_BLOCK), 733, 7),
            new ItemStackWithMaxMin(new ItemStack(Items.DIAMOND_BLOCK), 63, 2),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 350, 15)

    ),
    ULTIMATE(
            new ItemStackWithMaxMin(new ItemStack(Items.COAL_BLOCK), 1280, 64),
            new ItemStackWithMaxMin(new ItemStack(Items.IRON_BLOCK), 160, 16),
            new ItemStackWithMaxMin(new ItemStack(Items.GOLD_INGOT), 190, 19),
            new ItemStackWithMaxMin(new ItemStack(Items.NETHERITE_BLOCK), 10, 1),
            new ItemStackWithMaxMin(new ItemStack(Items.EXPERIENCE_BOTTLE), 1750, 35)
    );

    final ItemStackWithMaxMin[] stacks;

    TreasureRarities(ItemStackWithMaxMin... items) {
        stacks = items;
    }

    private static ItemStack getAndSetCount(ItemStack item, int count) {
        item.setCount(count);
        return item;
    }

    public ArrayList<ItemStack> rollStack(TreasureRarities rarity, PlayerEntity player) {
        ArrayList<ItemStack> rolled = new ArrayList<>();
        if (rarity == ULTIMATE) return ItemStackWithMaxMin.convert(ULTIMATE.stacks);
        else {
            for (ItemStackWithMaxMin i : rarity.stacks) {
                if (player.getRandom().nextDouble() <= 0.95) {
                    ItemStack item = i.getStack();
                    if (i.getMax() == i.getMin()) rolled.add(getAndSetCount(item, i.getMin()));
                    else {
                        int len = i.getMax() - i.getMin() + 1;
                        double pie = player.getRandom().nextDouble();
                        for (int b = 1; b<len; b++) {
                            double upper = Math.pow(2, (-b)+1);
                            double lower = Math.pow(2, -b);
                            if ((b+1) < len) lower = 0;
                            if ((upper >= pie) && (pie > lower)) {
                                rolled.add(getAndSetCount(item, b));
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
