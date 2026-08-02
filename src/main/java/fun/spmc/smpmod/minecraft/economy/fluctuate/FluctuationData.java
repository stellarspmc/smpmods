package fun.spmc.smpmod.minecraft.economy.fluctuate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class FluctuationData {

    public static final Codec<FluctuationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("mineral").forGetter(FluctuationData::getMineral),
            Codec.DOUBLE.fieldOf("default_price").forGetter(FluctuationData::getDefaultPrice),
            Codec.DOUBLE.fieldOf("fluctuation").forGetter(FluctuationData::getFluctuation),
            Codec.LONG.fieldOf("amount_deposit").forGetter(FluctuationData::getAmountDeposited),
            Codec.LONG.fieldOf("amount_withdraw").forGetter(FluctuationData::getAmountWithdrawn)
    ).apply(instance, FluctuationData::new));

    private final Item mineral;
    private final double defaultPrice;
    private final double fluctuation;
    private long amountDeposited;
    private long amountWithdrawn;

    private static final double SATURATION_VOLUME = 1000;
    private static final double BUY_MARGIN = 1.15;
    private static final double SELL_MARGIN = .85;

    public FluctuationData(Item mineral, double defaultPrice, double fluctuation, long amountDeposited, long amountWithdrawn) {
        this.mineral = mineral;
        this.defaultPrice = defaultPrice;
        this.fluctuation = fluctuation;
        this.amountDeposited = amountDeposited;
        this.amountWithdrawn = amountWithdrawn;
    }

    public FluctuationData(Item mineral, double defaultPrice, double fluctuation) {
        this(mineral, defaultPrice, fluctuation, 0, 0);
    }

    public Item getMineral() { return mineral; }
    public double getDefaultPrice() { return defaultPrice; }
    public double getFluctuation() { return fluctuation; }
    public long getAmountDeposited() { return amountDeposited; }
    public long getAmountWithdrawn() { return amountWithdrawn; }

    public double getBasePriceAt(long netDemand) {
        double priceShift = (netDemand / SATURATION_VOLUME) * fluctuation;
        double calculatedPrice = defaultPrice * (1.0 + priceShift);

        double minPrice = defaultPrice * .15;
        double maxPrice = defaultPrice * 4.5;

        double finalPrice = Math.clamp(calculatedPrice, minPrice, maxPrice);
        return Math.round(finalPrice * 100.0) / 100.0;
    }

    public double getCurrentPrice() {
        return getBasePriceAt(amountWithdrawn - amountDeposited);
    }

    public double getBulkBuyCost(int amount) {
        long currentNet = amountWithdrawn - amountDeposited;
        double startPrice = getBasePriceAt(currentNet) * BUY_MARGIN;
        double endPrice = getBasePriceAt(currentNet + amount) * BUY_MARGIN;

        double avgPrice = (startPrice + endPrice) / 2;
        return Math.round(avgPrice * amount * 100.0) / 100.0;
    }

    public double getBulkSellPayout(int amount) {
        long currentNet = amountWithdrawn - amountDeposited;
        double startPrice = getBasePriceAt(currentNet) * SELL_MARGIN;
        double endPrice = getBasePriceAt(currentNet - amount) * SELL_MARGIN;

        double avgPrice = (startPrice + endPrice) / 2.0;
        return Math.round(avgPrice * amount * 100.0) / 100.0;
    }

    public boolean deposit(long amount) {
        if (amount <= 0) return false;
        this.amountDeposited = Math.addExact(this.amountDeposited, amount);
        return true;
    }

    public boolean withdraw(long amount) {
        if (amount <= 0) return false;
        this.amountWithdrawn = Math.addExact(this.amountWithdrawn, amount);
        return true;
    }

    public void applyMarketDecay() {
        this.amountDeposited = (long) (this.amountDeposited * 0.9);
        this.amountWithdrawn = (long) (this.amountWithdrawn * 0.9);
    }
}