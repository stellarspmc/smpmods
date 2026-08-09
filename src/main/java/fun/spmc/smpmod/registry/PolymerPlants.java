package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.misc.ItemRarity;
import fun.spmc.smpmod.plant.crop.CropItem;
import fun.spmc.smpmod.plant.seed.SeedBlock;
import fun.spmc.smpmod.plant.seed.SeedItem;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;

public class PolymerPlants {
    public final static List<SeedItem> SEEDS = new ArrayList<>();

    private static void registerPlant(String cropId, Item baseCropItem, Item baseSeedItem, double basePrice, ItemRarity rarity) {
        CropItem cropItem = PolymerRegistry.createItem(cropId, properties -> new CropItem(properties, baseCropItem, MessageUtils.formatName(cropId), basePrice, rarity));
        SeedBlock seedBlock = (SeedBlock) PolymerRegistry.createBlockOnly(cropId + "_crop", properties -> new SeedBlock(properties, () -> cropItem), BlockBehaviour.Properties.of());
        SeedItem seedItem = PolymerRegistry.createItem(cropId + "_seeds", properties -> new SeedItem(seedBlock, properties, baseSeedItem, MessageUtils.formatName(cropId + "_seeds")));
        SEEDS.add(seedItem);
    }

    protected static void register() {
        registerPlant("wheat", Items.WHEAT, Items.WHEAT_SEEDS, 1f, ItemRarity.COMMON);
    }
}
