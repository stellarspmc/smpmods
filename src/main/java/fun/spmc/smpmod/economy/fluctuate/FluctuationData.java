package fun.spmc.smpmod.economy.fluctuate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
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
    private long lastTransactionTime = System.currentTimeMillis();

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
        double calculatedPrice = defaultPrice * (1 + priceShift);

        double finalPrice = Math.max(calculatedPrice, 0);
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

        double avgPrice = (startPrice + endPrice) / 2;
        return Math.round(avgPrice * amount * 100.0) / 100.0;
    }

    public void deposit(long amount) {
        if (amount <= 0) return;
        this.amountDeposited = Math.addExact(this.amountDeposited, amount);
        this.lastTransactionTime = System.currentTimeMillis();
    }

    public void withdraw(long amount) {
        if (amount <= 0) return;
        this.amountWithdrawn = Math.addExact(this.amountWithdrawn, amount);
        this.lastTransactionTime = System.currentTimeMillis();
    }

    public boolean applyMarketDecay(RandomSource source) {
        if ((System.currentTimeMillis() - this.lastTransactionTime) >= 150000 && (amountDeposited > 0 || amountWithdrawn > 0)) {
            amountDeposited = processFluctuation(amountDeposited, source.nextFloat() < 0.60f);
            amountWithdrawn = processFluctuation(amountWithdrawn, source.nextFloat() < 0.60f);
            return true;
        }
        return false;
    }

    private long processFluctuation(long currentAmount, boolean moveTowardsBase) {
        if (currentAmount < 0) return 0;
        long newAmount;
        if (moveTowardsBase) {
            if (currentAmount <= 30) return currentAmount + 1;
            newAmount = (long) (currentAmount * 0.99);
            if (newAmount == currentAmount) newAmount--;
        } else {
            newAmount = (long) (currentAmount * 1.01);
            if (newAmount == currentAmount) newAmount++;
            newAmount = Math.min(100000L, newAmount);
        }

        return Math.max(0, newAmount);
    }
}