package fun.spmc.smpmod.minecraft.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.discord.utils.MarkdownParser;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.stream.Collectors;

public class EconomySavedData extends SavedData {

    private static final Codec<Map<UUID, Double>> BALANCES_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.DOUBLE);

    private static final Codec<Map<UUID, String>> NAMES_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING);

    public static final Codec<EconomySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BALANCES_CODEC.fieldOf("balances").forGetter(data -> data.balances),
            NAMES_CODEC.fieldOf("names").forGetter(data -> data.names)
    ).apply(instance, EconomySavedData::new));

    public static final SavedDataType<EconomySavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmods", "economy"),
            EconomySavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, Double> balances;
    private final Map<UUID, String> names;

    public EconomySavedData(Map<UUID, Double> balances, Map<UUID, String> names) {
        this.balances = new HashMap<>(balances);
        this.names = new HashMap<>(names);
    }

    public EconomySavedData() {
        this(new HashMap<>(), new HashMap<>());
    }

    public static EconomySavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
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
        if (money >= 0 && money < Integer.MAX_VALUE) {
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
            String name = MarkdownParser.escapeMarkdown(resolveName(entry.getKey()));

            rankings.append(String.format("`#%02d` **%s** • $%,.2f\n", i + 1, name, entry.getValue()));
        }

        return rankings.toString();
    }

    private List<Map.Entry<UUID, Double>> getSortedBalances() {
        return balances.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
    }
}