package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.plant.CropItem;
import fun.spmc.smpmod.plant.SeedBlock;
import fun.spmc.smpmod.plant.SeedItem;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;

public class PolymerPlants {
    public final static List<SeedItem> SEEDS = new ArrayList<>();

    private static Item getBaseSeed(Item baseCrop) {
        if (baseCrop == Items.WHEAT) return Items.WHEAT_SEEDS;
        if (baseCrop == Items.CARROT) return Items.CARROT;
        if (baseCrop == Items.POTATO) return Items.POTATO;
        if (baseCrop == Items.BEETROOT) return Items.BEETROOT_SEEDS;
        if (baseCrop == Items.TORCHFLOWER) return Items.TORCHFLOWER_SEEDS;
        if (baseCrop == Items.MELON) return Items.MELON_SEEDS;
        if (baseCrop == Items.PUMPKIN) return Items.PUMPKIN_SEEDS;
        return Items.AIR;
    }
    private static void registerPlant(String cropId, Item baseCrop, double basePrice, ItemRarity rarity) {
        Item baseSeed = getBaseSeed(baseCrop);

        CropItem cropItem = PolymerRegistry.createItem(cropId, properties -> new CropItem(properties, baseCrop, MessageUtils.formatName(cropId), basePrice, rarity));
        SeedBlock seedBlock = (SeedBlock) PolymerRegistry.createBlockOnly(cropId + "_crop", properties -> new SeedBlock(properties, () -> cropItem), BlockBehaviour.Properties.of());
        SeedItem seedItem = PolymerRegistry.createItem(cropId + "_seeds", properties -> new SeedItem(seedBlock, properties, baseSeed, MessageUtils.formatName(cropId + "_seeds")));
        SEEDS.add(seedItem);
    }

    protected static void register() {
        registerPlant("wheat", Items.WHEAT, 1f, ItemRarity.COMMON);
        registerPlant("beetroot", Items.BEETROOT, 2f, ItemRarity.COMMON);
    }
}
