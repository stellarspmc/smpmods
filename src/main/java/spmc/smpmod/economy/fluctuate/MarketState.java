package spmc.smpmod.economy.fluctuate;

import com.mojang.serialization.Codec;
import spmc.smpmod.economy.EconomyData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spmc.smpmod.SMPMod.minecraftServer;

public class MarketState extends SavedData {
    private static final Map<Item, FluctuationData> permanentMarketMap = new HashMap<>();
    private static final Map<Item, FluctuationExpiry> temporaryMarketMap = new HashMap<>();
    private static int rotationTick = 144000;
    public static final Codec<MarketState> CODEC = FluctuationData.CODEC.listOf().xmap(
            datum -> {
                MarketState market = new MarketState();
                for (FluctuationData data : datum) market.registerMineral(data.getMineral(), data.getDefaultPrice(), data.getFluctuation());
                return market;
            }, _ -> List.copyOf(permanentMarketMap.values())
    );

    public static final SavedDataType<MarketState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "market"),
            MarketState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public MarketState() {}
    public FluctuationData get(Item item) { return getAll().get(item); }
    public static MarketState getState() { return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE); }
    public Map<Item, FluctuationData> getAll() {
        Map<Item, FluctuationData> combined = new HashMap<>(permanentMarketMap);
        temporaryMarketMap.forEach((item, expiry) -> combined.put(item, expiry.data()));
        return combined;
    }

    public void registerMineral(Item item, double defaultPrice, double fluctuation) {
        FluctuationData data = permanentMarketMap.computeIfAbsent(item, _ -> new FluctuationData(item, defaultPrice, fluctuation));
        data.defaultPrice = defaultPrice;
        data.fluctuation = fluctuation;
        setDirty();
    }

    public static double buyMineral(ServerPlayer player, Item item, int amount) {
        MarketState market = getState();
        FluctuationData data = market.get(item);
        if (amount <= 0) return -2;

        EconomyData eco = EconomyData.get();
        if (item.equals(Items.DIAMOND)) {
            if (!eco.changeBalance(player.getUUID(), -amount * 100)) return -1;
            return amount * 100;
        } else if (data == null) return -2;

        double totalCost = Math.round(data.getBulkBuyCost(amount) * 100.0) / 100.0;
        if (!eco.changeBalance(player.getUUID(), -totalCost)) return -1;
        data.withdraw(amount);
        market.setDirty();
        return totalCost;
    }

    public static double sellMineral(ServerPlayer player, Item item, int amount, double multiplier) {
        MarketState market = getState();
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

    public static void register() {
        MarketState market = getState();

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
    }

    protected static final List<FluctuationData> chosenItems = List.of(
            new FluctuationData(Items.ENCHANTED_GOLDEN_APPLE, 1500, 4.5),
            new FluctuationData(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 750, 2.5),
            new FluctuationData(Items.TOTEM_OF_UNDYING, 350, 1.5),
            new FluctuationData(Items.SHULKER_SHELL, 1200, 4)
    );

    public static void addTemporaryItem(MinecraftServer server) {
        if (chosenItems.isEmpty()) return;
        FluctuationData template = chosenItems.get(server.overworld().getRandom().nextInt(chosenItems.size()));
        temporaryMarketMap.put(template.getMineral(), new FluctuationExpiry(new FluctuationData(template.getMineral(), template.defaultPrice, template.fluctuation), server.getTickCount() + server.overworld().getRandom().nextInt(144000) + 144000));
    }


    public static void serverTickLoop(MinecraftServer server) {
        int ticks = server.getTickCount();
        if (ticks % 900 + (server.getPlayerList().getPlayerCount() - 1) * 125 == 0) {
            MarketState market = getState();
            boolean updated = false;
            for (FluctuationData data : permanentMarketMap.values()) if (data.applyMarketDecay(server.overworld().getRandom())) updated = true;
            if (!temporaryMarketMap.isEmpty()) temporaryMarketMap.values().forEach(data -> data.data().applyMarketDecay(server.overworld().getRandom()));
            if (updated) market.setDirty();
        }

        if (ticks % 1200 == 0 && !temporaryMarketMap.isEmpty()) temporaryMarketMap.values().removeIf(data -> ticks >= data.expiryTick());
        else if (ticks % rotationTick == 0) {
            addTemporaryItem(server);
            rotationTick = server.overworld().getRandom().nextInt(144000) + 144000;
        }
    }

    public static double processItemDeposit(ServerPlayer player, ItemStack stack) {
        Item baseItem = switch (stack.getItem().getDescriptionId()) {
            case "block.minecraft.netherite_block" -> Items.NETHERITE_INGOT;
            case "block.minecraft.diamond_block" -> Items.DIAMOND;
            case "block.minecraft.gold_block" -> Items.GOLD_INGOT;
            case "block.minecraft.emerald_block" -> Items.EMERALD;
            case "block.minecraft.lapis_block" -> Items.LAPIS_LAZULI;
            case "block.minecraft.iron_block" -> Items.IRON_INGOT;
            case "block.minecraft.copper_block" -> Items.COPPER_INGOT;
            case "block.minecraft.redstone_block" -> Items.REDSTONE;
            default -> stack.getItem();
        };
        return sellMineral(player, baseItem, stack.getCount() * ((baseItem != stack.getItem()) ? 9 : 1), (baseItem != stack.getItem()) ? .93 : 1);
    }

    public record FluctuationExpiry(FluctuationData data, int expiryTick) {}
}