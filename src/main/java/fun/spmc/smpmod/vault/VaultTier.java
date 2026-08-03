package fun.spmc.smpmod.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.BiConsumer;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public enum VaultTier implements StringRepresentable {
    ALPHA(
            "alpha",
            100000,
            List.of(
                    new ActivePerk(PerkType.BONUS_SHARPNESS, 1),
                    new ActivePerk(PerkType.EXTRA_HEARTS, 2)
            ),
            List.of(
                    new ConfiguredEvent(EventType.TREASURE_RARITY_LUCK, 1.0), // +100%
                    new ConfiguredEvent(EventType.HASTE_BUFF, 1.0),          // Haste I
                    new ConfiguredEvent(EventType.DEPOSIT_MONEY_BOOST, 0.05) // +5%
            )
    ),
    BETA(
            "beta",
            275000,
            List.of(
                    new ActivePerk(PerkType.BONUS_EFFICIENCY, 1),
                    new ActivePerk(PerkType.EXTRA_HEARTS, 3),
                    new ActivePerk(PerkType.MAX_HOMES, 1),
                    new ActivePerk(PerkType.BONUS_PROTECTION, 1)
            ),
            List.of(
                    new ConfiguredEvent(EventType.POTION_BUFF_BOOST, 0.25),
                    new ConfiguredEvent(EventType.RESISTANCE_BUFF, 2.0),
                    new ConfiguredEvent(EventType.BLOCK_TREASURE_RATE, 0.10)
            )
    ),
    DELTA(
            "delta",
            695000,
            List.of(
                    new ActivePerk(PerkType.EXTRA_HEARTS, 4)
            ),
            List.of(
                    new ConfiguredEvent(EventType.POTION_BUFF_BOOST, 0.30),
                    new ConfiguredEvent(EventType.TREASURE_ALWAYS_RARE, 1.0),
                    new ConfiguredEvent(EventType.BLOCK_TREASURE_RATE, 0.15),
                    new ConfiguredEvent(EventType.DEPOSIT_MONEY_BOOST, 0.10),
                    new ConfiguredEvent(EventType.LUCK_EFFECT, 3.0),
                    new ConfiguredEvent(EventType.HASTE_BUFF, 2.0)
            )
    ),
    GAMMA(
            "gamma",
            1012500,
            List.of(
                    new ActivePerk(PerkType.BONUS_SHARPNESS, 2),
                    new ActivePerk(PerkType.BONUS_EFFICIENCY, 2),
                    new ActivePerk(PerkType.EXTRA_HEARTS, 5),
                    new ActivePerk(PerkType.BONUS_PROTECTION, 2),
                    new ActivePerk(PerkType.MAX_HOMES, 2),
                    new ActivePerk(PerkType.GATHERING_INCOME, 1)
            ),
            List.of(
                    new ConfiguredEvent(EventType.BLOCK_TREASURE_RATE, 0.20),
                    new ConfiguredEvent(EventType.RPG_MOB_DROP_LUCK, 1.5),
                    new ConfiguredEvent(EventType.MACHINE_SPEED_BOOST, 2.0),
                    new ConfiguredEvent(EventType.PLANT_BUFFY_DISCOUNT, 0.25),
                    new ConfiguredEvent(EventType.RESISTANCE_BUFF, 4.0)
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

    public record ActivePerk(PerkType type, int level) implements VaultEntry {

        public static final Codec<ActivePerk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PerkType.CODEC.fieldOf("type").forGetter(ActivePerk::type),
                Codec.INT.fieldOf("level").forGetter(ActivePerk::level)
        ).apply(instance, ActivePerk::new));

        @Override
        public String id() {
            return type.getSerializedName();
        }

        @Override
        public double value() {
            return level;
        }

        @Override
        public void apply(ServerLevel serverLevel) {
            minecraftServer.getPlayerList().getPlayers().forEach((player -> type.trigger(level, player)));
        }
    }

    public record ConfiguredEvent(EventType type, double modifier) implements VaultEntry {

        public static final Codec<ConfiguredEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EventType.CODEC.fieldOf("type").forGetter(ConfiguredEvent::type),
                Codec.DOUBLE.fieldOf("modifier").forGetter(ConfiguredEvent::modifier)
        ).apply(instance, ConfiguredEvent::new));

        @Override
        public String id() {
            return type.getSerializedName();
        }

        @Override
        public double value() {
            return modifier;
        }

        @Override
        public void apply(ServerLevel level) {
            type.trigger(modifier, level);
        }
    }

    enum EventType implements StringRepresentable {
        TREASURE_RARITY_LUCK("treasure_rarity_luck", 1440, (value, level) -> {}), // 24 Hours
        HASTE_BUFF("haste_buff", 720, (amplifier, level) -> {}),                   // 12 Hours (Haste I / II)
        DEPOSIT_MONEY_BOOST("deposit_money_boost", 60, (boostPercent, level) -> {}), // 1 Hour

        POTION_BUFF_BOOST("potion_buff_boost", 720, (percent, level) -> {}),     // 12 Hours
        RESISTANCE_BUFF("resistance_buff", 1440, (amplifier, level) -> {}),      // 24 Hours / 12 Hours
        BLOCK_TREASURE_RATE("block_treasure_rate", 120, (rateBonus, level) -> {}),// 2 Hours

        TREASURE_ALWAYS_RARE("treasure_always_rare", 15, (unused, level) -> {}), // 15 Mins
        LUCK_EFFECT("luck_effect", 120, (amplifier, level) -> {}),               // 2 Hours

        RPG_MOB_DROP_LUCK("rpg_mob_drop_luck", 180, (multiplier, level) -> {}),  // 3 Hours
        MACHINE_SPEED_BOOST("machine_speed_boost", 120, (multiplier, level) -> {}),// 2 Hours
        PLANT_BUFFY_DISCOUNT("plant_buffy_discount", 360, (discount, level) -> {});// 6 Hours

        public static final Codec<EventType> CODEC = StringRepresentable.fromEnum(EventType::values);

        private final String name;
        private final int durationMinutes;
        private final BiConsumer<Double, ServerLevel> startCallback;

        EventType(String name, int durationMinutes, BiConsumer<Double, ServerLevel> startCallback) {
            this.name = name;
            this.durationMinutes = durationMinutes;
            this.startCallback = startCallback;
        }

        public void trigger(double modifierValue, ServerLevel level) {
            startCallback.accept(modifierValue, level);
        }

        public int getDurationMinutes() { return durationMinutes; }

        @Override public @NonNull String getSerializedName() { return this.name; }
    }

    enum PerkType implements StringRepresentable {
        EXTRA_HEARTS("extra_hearts", (tierLevel, player) -> {
            // e.g., apply generic.max_health attribute modifier based on tierLevel (+2, +3, +4, +5 hearts)
        }),
        BONUS_SHARPNESS("bonus_sharpness", (tierLevel, player) -> {
            // e.g., apply passive sharpness bonus to player attacks
        }),
        BONUS_EFFICIENCY("bonus_efficiency", (tierLevel, player) -> {
            // e.g., apply passive efficiency bonus
        }),
        BONUS_PROTECTION("bonus_protection", (tierLevel, player) -> {
            // e.g., apply generic.armor / protection modifier
        }),
        MAX_HOMES("max_homes", (tierLevel, player) -> {
            // e.g., update player's home limit in home manager (+1, +2)
        }),
        GATHERING_INCOME("gathering_income", (tierLevel, player) -> {
            // e.g., +50% plant and fishing selling multiplier
        });

        public static final Codec<PerkType> CODEC = StringRepresentable.fromEnum(PerkType::values);

        private final String name;
        private final BiConsumer<Integer, ServerPlayer> applyCallback;

        PerkType(String name, BiConsumer<Integer, ServerPlayer> applyCallback) {
            this.name = name;
            this.applyCallback = applyCallback;
        }

        public void trigger(int tierLevel, ServerPlayer player) {
            applyCallback.accept(tierLevel, player);
        }

        @Override public @NonNull String getSerializedName() { return this.name; }
    }

}