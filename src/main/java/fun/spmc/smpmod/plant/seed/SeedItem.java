package fun.spmc.smpmod.plant.seed;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

public class SeedItem extends PolymerBlockItem {
    private final Item vanillaItem;
    private final String seedName;

    public SeedItem(Block block, Properties settings, Item item, String seedName) {
        super(block, settings);
        vanillaItem = item;
        this.seedName = seedName;
    }

    @Override public Item getPolymerItem(ItemStack itemStack, PacketContext context) { return vanillaItem; }
    public String getSeedName() { return seedName; }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        CustomData customData = out.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag rootTag = customData.copyTag();
    }
}
