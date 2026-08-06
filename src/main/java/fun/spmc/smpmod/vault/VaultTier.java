package fun.spmc.smpmod.vault;

import com.mojang.serialization.Codec;
import fun.spmc.smpmod.vault.entries.ActivePerk;
import fun.spmc.smpmod.vault.entries.ConfiguredEvent;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public enum VaultTier implements StringRepresentable {
    ALPHA(
            "alpha",
            250000,
            List.of(
                    new ActivePerk(ActivePerk.PerkType.BONUS_SHARPNESS, 1),
                    new ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 1),
                    new ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 1)
            ),
            List.of(
                    new ConfiguredEvent(ConfiguredEvent.EventType.TREASURE_RARITY_LUCK, 1),
                    new ConfiguredEvent(ConfiguredEvent.EventType.HASTE_BUFF, 1),
                    new ConfiguredEvent(ConfiguredEvent.EventType.DEPOSIT_MONEY_BOOST, .05),
                    new ConfiguredEvent(ConfiguredEvent.EventType.EXTENDED_EFFECT_DURATION, .25)
            )
    ),
    BETA(
            "beta",
            425000,
            List.of(
                    new ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 2),
                    new ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 2),
                    new ActivePerk(ActivePerk.PerkType.MAX_HOMES, 1),
                    new ActivePerk(ActivePerk.PerkType.BONUS_PROTECTION, 1)
            ),
            List.of(
                    new ConfiguredEvent(ConfiguredEvent.EventType.RESISTANCE_BUFF, 2.0),
                    new ConfiguredEvent(ConfiguredEvent.EventType.BLOCK_TREASURE_RATE, 0.10)
            )
    ),
    DELTA(
            "delta",
            695000,
            List.of(
                    new ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 3),
                    new ActivePerk(ActivePerk.PerkType.BONUS_PROTECTION, 2)
            ),
            List.of(
                    new ConfiguredEvent(ConfiguredEvent.EventType.TREASURE_ALWAYS_RARE, 1),
                    new ConfiguredEvent(ConfiguredEvent.EventType.BLOCK_TREASURE_RATE, .15),
                    new ConfiguredEvent(ConfiguredEvent.EventType.DEPOSIT_MONEY_BOOST, .03),
                    new ConfiguredEvent(ConfiguredEvent.EventType.LUCK_EFFECT, 3),
                    new ConfiguredEvent(ConfiguredEvent.EventType.HASTE_BUFF, 2)
            )
    ),
    GAMMA(
            "gamma",
            1012500,
            List.of(
                    new ActivePerk(ActivePerk.PerkType.BONUS_SHARPNESS, 2),
                    new ActivePerk(ActivePerk.PerkType.BONUS_EFFICIENCY, 2),
                    new ActivePerk(ActivePerk.PerkType.EXTRA_HEARTS, 5),
                    new ActivePerk(ActivePerk.PerkType.BONUS_PROTECTION, 2),
                    new ActivePerk(ActivePerk.PerkType.MAX_HOMES, 2),
                    new ActivePerk(ActivePerk.PerkType.GATHERING_INCOME, 1)
            ),
            List.of(
                    new ConfiguredEvent(ConfiguredEvent.EventType.BLOCK_TREASURE_RATE, .2),
                    new ConfiguredEvent(ConfiguredEvent.EventType.RPG_MOB_DROP_LUCK, 1.5),
                    new ConfiguredEvent(ConfiguredEvent.EventType.MACHINE_SPEED_BOOST, 2),
                    new ConfiguredEvent(ConfiguredEvent.EventType.PLANT_BUFFY_DISCOUNT, .25),
                    new ConfiguredEvent(ConfiguredEvent.EventType.RESISTANCE_BUFF, 4),
                    new ConfiguredEvent(ConfiguredEvent.EventType.DEPOSIT_MONEY_BOOST, .06)
            )
    );

    public static final Codec<VaultTier> CODEC = StringRepresentable.fromEnum(VaultTier::values);

    private final String name;
    private final double costGoal;
    private final List<ActivePerk> perks;
    private final List<ConfiguredEvent> eventPool;

    VaultTier(String name, double costGoal, List<ActivePerk> perks, List<ConfiguredEvent> eventPool) {
        this.name = name;
        this.costGoal = costGoal;
        this.perks = perks;
        this.eventPool = eventPool;
    }

    public String getName() { return name; }
    public double getCostGoal() { return costGoal; }
    public List<ActivePerk> getPerks() { return perks; }
    public List<ConfiguredEvent> getEventPool() { return eventPool; }

    public VaultTier getNextTier() {
        VaultTier[] values = values();
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < values.length ? values[nextOrdinal] : this;
    }

    @Override public @NonNull String getSerializedName() { return this.name; }
}