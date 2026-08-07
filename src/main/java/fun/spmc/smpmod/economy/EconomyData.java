package fun.spmc.smpmod.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.stream.Collectors;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class EconomyData extends SavedData {

    private static final Codec<Map<UUID, Double>> BALANCES_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.DOUBLE);

    private static final Codec<Map<UUID, String>> NAMES_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING);

    public static final Codec<EconomyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BALANCES_CODEC.fieldOf("balances").forGetter(data -> data.balances),
            NAMES_CODEC.fieldOf("names").forGetter(data -> data.names)
    ).apply(instance, EconomyData::new));

    public static final SavedDataType<EconomyData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmods", "economy"),
            EconomyData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, Double> balances;
    private final Map<UUID, String> names;

    public EconomyData(Map<UUID, Double> balances, Map<UUID, String> names) {
        this.balances = new HashMap<>(balances);
        this.names = new HashMap<>(names);
    }

    public EconomyData() {
        this(new HashMap<>(), new HashMap<>());
    }

    public static EconomyData get() {
        return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void registerPlayer(UUID uuid, String name) {
        if (!balances.containsKey(uuid)) {
            balances.put(uuid, 0d);
        }
        names.put(uuid, name);
        this.setDirty();
    }

    public String resolveName(UUID uuid) {
        return names.getOrDefault(uuid, uuid.toString().substring(0, 8));
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0d);
    }

    public void setBalance(UUID uuid, double money) {
        if (money >= 0 && money < Double.MAX_VALUE) {
            balances.put(uuid, money);
            this.setDirty();
        }
    }

    public boolean changeBalance(UUID uuid, double money) {
        double current = getBalance(uuid);
        if (current + money >= 0 && current + money < Double.MAX_VALUE) {
            balances.put(uuid, current + money);
            this.setDirty();
            return true;
        }
        return false;
    }

    public String top(int page) {
        List<Map.Entry<UUID, Double>> sorted = getSortedBalances();
        List<Map.Entry<UUID, Double>> filtered = sorted.stream().filter(entry -> !Objects.equals(resolveName(entry.getKey()), "spmc")).toList();
        StringBuilder rankings = new StringBuilder();
        int pageSize = 10;
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());

        if (startIndex >= filtered.size() || startIndex < 0) return "*No data available for this page.*";
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<UUID, Double> entry = filtered.get(i);
            String name = MarkdownSanitizer.escape(resolveName(entry.getKey()));

            rankings.append(String.format("`#%02d` **%s** • $%,.2f\n", i + 1, name, entry.getValue()));
        }

        return rankings.toString();
    }

    public Component getMinecraftTop(int page) {
        List<Map.Entry<UUID, Double>> sorted = getSortedBalances();
        List<Map.Entry<UUID, Double>> filtered = sorted.stream()
                .filter(entry -> !Objects.equals(resolveName(entry.getKey()), "spmc"))
                .toList();

        int pageSize = 10;
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());

        if (startIndex >= filtered.size() || startIndex < 0) {
            return Component.literal("No data available for this page.")
                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);
        }

        MutableComponent rankings = Component.empty();

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<UUID, Double> entry = filtered.get(i);

            // Do NOT use MessageUtils.escapeMarkdown() here for Minecraft
            String name = resolveName(entry.getKey());

            // Format: #01 name • $500.00
            MutableComponent line = Component.literal(String.format("#%02d ", i + 1)).withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(name).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .append(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.format("$%,.2f", entry.getValue())).withStyle(ChatFormatting.GREEN));

            rankings.append(line);

            if (i < endIndex - 1) rankings.append("\n");
        }

        return rankings;
    }

    private List<Map.Entry<UUID, Double>> getSortedBalances() {
        return balances.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
    }
}