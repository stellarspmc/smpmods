package ml.spmc.smpmod.minecraft.economy;

public enum EconomyValues {
    copper(0.1),
    emerald(0.2),
    coal(0.5),
    iron(1),
    gold(2),
    diamond(5),
    netherite(10),
    nether_star(50);

    double value;

    EconomyValues(double value) {
        this.value = value;
    }

    /**
     * @param value update value of ores, used in 1.6
     */
    void update(double value) {
        this.value += value;
    }
}
