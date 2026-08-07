package fun.spmc.smpmod.economy.fluctuate;

import com.mojang.serialization.Codec;
import fun.spmc.smpmod.economy.EconomyData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class MarketState extends SavedData {
    private final Map<Item, FluctuationData> marketMap = new HashMap<>();

    public static final Codec<MarketState> CODEC = FluctuationData.CODEC.listOf().xmap(
            datum -> {
                MarketState market = new MarketState();
                for (FluctuationData data : datum) market.registerMineral(data.getMineral(), data.getDefaultPrice(), data.getFluctuation());
                return market;
            },
            market -> List.copyOf(market.marketMap.values())
    );

    public static final SavedDataType<MarketState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "market"),
            MarketState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public MarketState() {}

    public FluctuationData get(Item item) {
        return marketMap.get(item);
    }

    public Map<Item, FluctuationData> getAll() {
        return marketMap;
    }

    public void registerMineral(Item item, double defaultPrice, double fluctuation) {
        FluctuationData data = marketMap.get(item);
        if (data == null) marketMap.put(item, new FluctuationData(item, defaultPrice, fluctuation));
        else {
            data.defaultPrice = defaultPrice;
            data.fluctuation = fluctuation;
        }

        setDirty();
    }

    public static MarketState getState() {
        return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static double buyMineral(ServerPlayer player, Item item, int amount) {
        MarketState market = MarketState.getState();
        FluctuationData data = market.get(item);
        if (data == null || amount <= 0) return -2;

        double totalCost = Math.round(data.getBulkBuyCost(amount) * 100.0) / 100.0;
        EconomyData eco = EconomyData.get();

        if (!eco.changeBalance(player.getUUID(), -totalCost)) return -1;
        data.withdraw(amount);
        market.setDirty();
        return totalCost;
    }

    public static double sellMineral(ServerPlayer player, Item item, int amount, double multiplier) {
        MarketState market = MarketState.getState();
        FluctuationData data = market.get(item);
        EconomyData eco = EconomyData.get();

        if (item == Items.DIAMOND) return eco.changeBalance(player.getUUID(), 100 * amount * multiplier) ? 100 * amount * multiplier : 0;
        if (data == null || amount <= 0) return 0;

        double totalPayout = Math.round(data.getBulkSellPayout(amount) * multiplier * 100.0) / 100.0;
        if (totalPayout <= 0) return 0;

        if (eco.changeBalance(player.getUUID(), totalPayout)) {
            data.deposit(amount);
            market.setDirty();
            return totalPayout;
        }

        return 0;
    }

    private static int tickCounter = 0;

    public static void register() {
        MarketState market = MarketState.getState();

        market.registerMineral(Items.HEART_OF_THE_SEA, 2000, 6);
        market.registerMineral(Items.NETHER_STAR, 1250, 3);
        market.registerMineral(Items.NETHERITE_INGOT, 750, 2.15);
        market.registerMineral(Items.ECHO_SHARD, 50, .75);
        market.registerMineral(Items.GOLD_INGOT, 10, .5);
        market.registerMineral(Items.EMERALD, 5, .25);
        market.registerMineral(Items.IRON_INGOT, 2, .35);
        market.registerMineral(Items.LAPIS_LAZULI, 1, .45);
        market.registerMineral(Items.REDSTONE, .5, .5);
        market.registerMineral(Items.COPPER_INGOT, .2, .75);
        market.registerMineral(Items.COAL, .1, 1.95);
        market.registerMineral(Items.AMETHYST_SHARD, .05, 2.15);

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            int playerCount = server.getPlayerList().getPlayerCount();
            if (playerCount == 0) return;
            int targetInterval = 900 + (playerCount - 1) * 125;
            tickCounter++;

            if (tickCounter >= targetInterval) {
                tickCounter = 0;

                boolean updated = false;
                for (FluctuationData data : market.marketMap.values()) {
                    if (data.applyMarketDecay(server.overworld().getRandom())) updated = true;
                }

                if (updated) market.setDirty();
            }
        });
    }
}