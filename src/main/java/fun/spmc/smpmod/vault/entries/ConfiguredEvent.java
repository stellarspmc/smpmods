package fun.spmc.smpmod.vault.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.economy.fluctuate.FluctuationData;
import fun.spmc.smpmod.misc.NPCData;
import fun.spmc.smpmod.treasure.TreasureEvents;
import fun.spmc.smpmod.vault.VaultUtils;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class ConfiguredEvent implements VaultEntry {

    public static final Codec<ConfiguredEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EventType.CODEC.fieldOf("type").forGetter(ConfiguredEvent::type),
            Codec.DOUBLE.fieldOf("modifier").forGetter(ConfiguredEvent::modifier),
            Codec.INT.optionalFieldOf("remaining_ticks", -1).forGetter(ConfiguredEvent::getRemainingTicks)
    ).apply(instance, ConfiguredEvent::new));

    private final EventType type;
    private final double modifier;
    private int remainingTicks;

    public ConfiguredEvent(EventType type, double modifier, int remainingTicks) {
        this.type = type;
        this.modifier = modifier;
        this.remainingTicks = remainingTicks == -1 ? type.getDurationTick() : remainingTicks;
    }

    public ConfiguredEvent(EventType type, double modifier) {
        this(type, modifier, -1);
    }

    public EventType type() {
        return type;
    }

    public double modifier() {
        return modifier;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

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
        if (NPCData.get().getMannequin(minecraftServer.overworld(), "vault_guardian") != null) type.trigger(modifier, level);
    }

    public boolean tick(ServerLevel level) {
        if (NPCData.get().getMannequin(minecraftServer.overworld(), "vault_guardian") != null) {
            this.remainingTicks--;
            if (this.remainingTicks % 20 == 0) type.triggerTick(modifier, level);
            if (this.remainingTicks <= 0) type.triggerEnd(level);
            return this.remainingTicks <= 0;
        } return false;
    }

    @Override
    public String toString() {
        String[] words = type.getSerializedName().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        String timeFormatted = formatTime(remainingTicks);
        if (modifier > 1) return String.format("%s (+%.0f%%) - %s", formattedName.toString().trim(), (modifier - 1.0) * 100, timeFormatted);
        return String.format("%s - %s", formattedName.toString().trim(), timeFormatted);
    }

    private String formatTime(int ticks) {
        if (ticks <= 0) return "Expired";

        int totalSeconds = ticks / 20;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public enum EventType implements StringRepresentable {
        TREASURE_RARITY_LUCK("treasure_rarity_luck", 1440 * 20 * 60, (value, level) -> {
        }),
        HASTE_BUFF("haste_buff", 720 * 20 * 60, (amplifier, level) -> level.getServer().getPlayerList().getPlayers().forEach((player -> applyEffects(player, MobEffects.HASTE, amplifier.intValue())))),
        DEPOSIT_MONEY_BOOST("deposit_money_boost", 60 * 20 * 60, (boostPercent, _) -> FluctuationData.changeMargin(1 - boostPercent), (_) -> FluctuationData.changeMargin(1)),
        RESISTANCE_BUFF("resistance_buff", 720 * 20 * 60, (amplifier, level) -> level.getServer().getPlayerList().getPlayers().forEach((player -> applyEffects(player, MobEffects.RESISTANCE, amplifier.intValue())))),
        BLOCK_TREASURE_RATE("block_treasure_rate", 120 * 20 * 60, (rateBonus, _) -> TreasureEvents.eventPercentage = 1 + rateBonus, (_) -> TreasureEvents.eventPercentage = 1),
        TREASURE_ALWAYS_RARE("treasure_always_rare", 15 * 20 * 60, (_, _) -> TreasureEvents.rigTreasures = true, (_) -> TreasureEvents.rigTreasures = false),
        EXTENDED_EFFECT_DURATION("extended_effect_duration", 120 * 20 * 60, (amplifier, _) -> VaultUtils.buffValue = amplifier.floatValue(), (_) -> VaultUtils.buffValue = 0),
        LUCK_EFFECT("luck_effect", 120 * 20 * 60, (amplifier, level) -> level.getServer().getPlayerList().getPlayers().forEach((player -> applyEffects(player, MobEffects.LUCK, amplifier.intValue())))),
        RPG_MOB_DROP_LUCK("rpg_mob_drop_luck", 180 * 20 * 60, (multiplier, level) -> {
            // TODO: finish when rpg mobs are here
        }),
        MACHINE_SPEED_BOOST("machine_speed_boost", 120 * 20 * 60, (multiplier, level) -> {
            // TODO: make when slimefun implemented
        }),
        PLANT_BUFFY_DISCOUNT("plant_buffy_discount", 360 * 20 * 60, (discount, level) -> {
            // TODO: make when plant system is here
        });

        public static final Codec<EventType> CODEC = StringRepresentable.fromEnum(EventType::values);

        private final String name;
        private final BiConsumer<Double, ServerLevel> startCallback;
        private final BiConsumer<Double, ServerLevel> tickCallback;
        private final Consumer<ServerLevel> endCallback;
        private final int durationTick;

        // start and tick same func
        EventType(String name, int durationTick, BiConsumer<Double, ServerLevel> startCallback) {
            this(name, durationTick, startCallback, startCallback, (_) -> {});
        }

        // start, end
        EventType(String name, int durationTick, BiConsumer<Double, ServerLevel> startCallback, Consumer<ServerLevel> endCallback) {
            this(name, durationTick, startCallback, (_, _) -> {}, endCallback);
        }

        EventType(String name, int durationTick,
                  BiConsumer<Double, ServerLevel> startCallback,
                  BiConsumer<Double, ServerLevel> tickCallback,
                  Consumer<ServerLevel> endCallback) {
            this.name = name;
            this.durationTick = durationTick;
            this.startCallback = startCallback;
            this.tickCallback = tickCallback;
            this.endCallback = endCallback;
        }

        public void trigger(double modifierValue, ServerLevel level) { startCallback.accept(modifierValue, level); }
        public void triggerTick(double modifier, ServerLevel level) { tickCallback.accept(modifier, level); }
        public void triggerEnd(ServerLevel level) { endCallback.accept(level); }

        public int getDurationTick() {
            return durationTick;
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.name;
        }

        private static void applyEffects(ServerPlayer player, Holder<MobEffect> effect, int amp) {
            player.addEffect(new MobEffectInstance(effect, 40, amp - 1, false, false, true));
        }
    }
}