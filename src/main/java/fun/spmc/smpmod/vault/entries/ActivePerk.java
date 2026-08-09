package fun.spmc.smpmod.vault.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.misc.NPCData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

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
        if (NPCData.get().getMannequin(minecraftServer.overworld(), "vault_guardian") != null) minecraftServer.getPlayerList().getPlayers().forEach((player -> type.trigger(level, player)));
    }

    @Override
    public @NonNull String toString() {
        String[] words = type.getSerializedName().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return formattedName.toString().trim() + " " + toRomanNumeral(level);
    }

    private static String toRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "Lvl " + level;
        };
    }

     public enum PerkType implements StringRepresentable {
        EXTRA_HEARTS("extra_hearts", (tierLevel, player) -> {
            var attr = player.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) attr.addOrReplacePermanentModifier(new AttributeModifier(
                    Identifier.fromNamespaceAndPath("smpmod", "perk_extra_hearts"),
                    tierLevel * 2.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }),
        BONUS_SHARPNESS("bonus_sharpness", (tierLevel, player) -> {
            var attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.addOrReplacePermanentModifier(new AttributeModifier(
                        Identifier.fromNamespaceAndPath("smpmod", "perk_bonus_sharpness"),
                        tierLevel * 1.25,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }),
        BONUS_EFFICIENCY("bonus_efficiency", (tierLevel, player) -> {
            var attr = player.getAttribute(Attributes.MINING_EFFICIENCY);
            if (attr != null) {
                attr.addOrReplacePermanentModifier(new AttributeModifier(
                        Identifier.fromNamespaceAndPath("smpmod", "perk_bonus_efficiency"),
                        tierLevel * 2.0,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }),
        BONUS_PROTECTION("bonus_protection", (tierLevel, player) -> {
            var attr = player.getAttribute(Attributes.ARMOR);
            if (attr != null) {
                attr.addOrReplacePermanentModifier(new AttributeModifier(
                        Identifier.fromNamespaceAndPath("smpmod", "perk_bonus_protection"),
                        tierLevel * 2.0,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }),
        MAX_HOMES("max_homes", (tierLevel, player) -> {
            // TODO: later
        }),
        GATHERING_INCOME("gathering_income", (tierLevel, player) -> {
            // TODO: adding when fish is here
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
