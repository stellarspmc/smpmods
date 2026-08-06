package fun.spmc.smpmod.vault.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

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

    protected enum EventType implements StringRepresentable {
        TREASURE_RARITY_LUCK("treasure_rarity_luck", (value, level) -> {
            int durationMinutes = 1440;
        }),
        HASTE_BUFF("haste_buff", (amplifier, level) -> {
            int durationMinutes = 720;
        }),
        DEPOSIT_MONEY_BOOST("deposit_money_boost", (boostPercent, level) -> {
            int durationMinutes = 60;
        }),
        POTION_BUFF_BOOST("potion_buff_boost", (percent, level) -> {
            int durationMinutes = 720;
        }),
        RESISTANCE_BUFF("resistance_buff", (amplifier, level) -> {
            int durationMinutes = 720;
        }),
        BLOCK_TREASURE_RATE("block_treasure_rate", (rateBonus, level) -> {
            int durationMinutes = 120;
        }),
        TREASURE_ALWAYS_RARE("treasure_always_rare", (unused, level) -> {
            int durationMinutes = 15;
        }),
        LUCK_EFFECT("luck_effect", (amplifier, level) -> {
            int durationMinutes = 120;
        }),
        RPG_MOB_DROP_LUCK("rpg_mob_drop_luck", (multiplier, level) -> {
            int durationMinutes = 180;
            // TODO: finish when rpg mobs are here
        }),
        MACHINE_SPEED_BOOST("machine_speed_boost", (multiplier, level) -> {
            int durationMinutes = 120;
            // TODO: make when slimefun implemented
        }),
        PLANT_BUFFY_DISCOUNT("plant_buffy_discount", (discount, level) -> {
            int durationMinutes = 360;
            // TODO: make when plant system is here
        });

        public static final Codec<EventType> CODEC = StringRepresentable.fromEnum(EventType::values);

        private final String name;
        private final BiConsumer<Double, ServerLevel> startCallback;

        EventType(String name, BiConsumer<Double, ServerLevel> startCallback) {
            this.name = name;
            this.startCallback = startCallback;
        }

        public void trigger(double modifierValue, ServerLevel level) {
            startCallback.accept(modifierValue, level);
        }

        @Override public @NonNull String getSerializedName() { return this.name; }
    }
}