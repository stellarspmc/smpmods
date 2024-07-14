package fun.spmc.smpmod.minecraft.treasure;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class ItemStackWithMaxMin {
    ItemStack stack;
    int max;
    int min;
    public ItemStackWithMaxMin(ItemStack stack, int max, int min) {
        this.stack = stack;
        this.max = max;
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getMin() {
        return min;
    }

    public static ArrayList<ItemStack> convert(ItemStackWithMaxMin... items) {
        ArrayList<ItemStack> stack = new ArrayList<>();
        for (ItemStackWithMaxMin i: items) {
            stack.add(i.getStack());
        }
        return stack;
    }
}
