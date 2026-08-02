package fun.spmc.smpmod.minecraft.economy.fluctuate;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class MarketState extends SavedData {

    private final Map<Item, FluctuationData> marketMap = new HashMap<>();

    public FluctuationData get(Item item) {
        return marketMap.get(item);
    }

    public Map<Item, FluctuationData> getAll() {
        return marketMap;
    }

    public void registerMineral(Item item, double defaultPrice, double fluctuation) {
        marketMap.putIfAbsent(item, new FluctuationData(item, defaultPrice, fluctuation));
        setDirty();
    }
}