package fun.spmc.smpmod.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.misc.NPCData;
import fun.spmc.smpmod.vault.entries.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.stream.Collectors;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class VaultData extends SavedData {
    public static final Codec<VaultData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VaultTier.CODEC.fieldOf("vault_tier").forGetter(VaultData::getCurrentTier),
            Codec.DOUBLE.fieldOf("current_money").forGetter(VaultData::getCurrentMoney),
            Codec.list(ActivePerk.CODEC).fieldOf("perks").forGetter(VaultData::getActivePerks),
            Codec.list(ConfiguredEvent.CODEC).fieldOf("events").forGetter(VaultData::getActiveEvents),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.DOUBLE).optionalFieldOf("leaderboard", Map.of()).forGetter(VaultData::getLeaderboard)
    ).apply(instance, VaultData::new));

    public static final SavedDataType<VaultData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "vault"),
            VaultData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private double currentMoney = 0;
    private VaultTier currentTier = VaultTier.ALPHA;
    private final List<ActivePerk> activePerks = new ArrayList<>();
    private final List<ConfiguredEvent> activeEvents = new ArrayList<>();
    private final Map<UUID, Double> leaderboard = new HashMap<>();

    public VaultData() {}

    public VaultData(VaultTier currentTier, double currentMoney, List<ActivePerk> perks, List<ConfiguredEvent> events, Map<UUID, Double> leaderboard) {
        this.currentTier = currentTier;
        this.currentMoney = currentMoney;
        this.activePerks.addAll(perks);
        this.activeEvents.addAll(events);
        this.leaderboard.putAll(leaderboard);
    }

    public Map<UUID, Double> getLeaderboard() { return leaderboard; }
    public static VaultData get() { return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE); }

    public void recordDonation(UUID playerUuid, double amount) {
        this.leaderboard.merge(playerUuid, amount, Double::sum);
        this.addMoney(amount);
    }

    private void addMoney(double amount) {
        this.currentMoney += amount;

        boolean unlockedSomething = false;
        while (canAdvance()) {
            currentMoney = Math.max(0, currentMoney - currentTier.getCostGoal());
            advance();
            unlockedSomething = true;
        }

        if (unlockedSomething || amount > 0) setDirty();
    }

    private boolean canAdvance() {
        List<ConfiguredEvent> availableEvents = getAvailableEvents();
        List<ActivePerk> availablePerks = getAvailablePerks();
        boolean check = this.currentMoney >= currentTier.getCostGoal();

        if (availableEvents.isEmpty() && availablePerks.isEmpty()) return (this.currentTier != VaultTier.GAMMA) && check;
        return check;
    }

    private void advance() {
        ServerLevel level = minecraftServer.overworld();
        RandomSource random = level.getRandom();

        List<ConfiguredEvent> availableEvents = getAvailableEvents();
        List<ActivePerk> availablePerks = getAvailablePerks();

        if (availableEvents.isEmpty() && availablePerks.isEmpty()) {
            if (this.currentTier != VaultTier.GAMMA) this.currentTier = this.currentTier.getNextTier();
            return;
        }

        boolean pickEvent = random.nextBoolean();
        if (pickEvent && availableEvents.isEmpty()) pickEvent = false;
        if (!pickEvent && availablePerks.isEmpty()) pickEvent = true;

        VaultEntry entry;
        if (pickEvent) {
            entry = availableEvents.get(random.nextInt(availableEvents.size()));
            this.activeEvents.add((ConfiguredEvent) entry);
        } else {
            entry = availablePerks.get(random.nextInt(availablePerks.size()));

            this.activePerks.removeIf(p -> p.type() == ((ActivePerk) entry).type());
            this.activePerks.add((ActivePerk) entry);
        }
        entry.apply(level);
    }

    private List<ConfiguredEvent> getAvailableEvents() {
        return currentTier.getEventPool().stream().filter(e -> !this.activeEvents.contains(e)).toList();
    }

    private List<ActivePerk> getAvailablePerks() {
        return currentTier.getPerks().stream().filter(tierPerk -> this.activePerks.stream().noneMatch(activePerk ->
                activePerk.type() == tierPerk.type() && activePerk.level() >= tierPerk.level())).toList();
    }

    public double getCurrentMoney() { return currentMoney; }
    public VaultTier getCurrentTier() { return currentTier; }
    public List<ActivePerk> getActivePerks() { return activePerks; }
    public List<ConfiguredEvent> getActiveEvents() { return activeEvents; }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            for (ActivePerk perk : VaultData.get().getActivePerks()) if (NPCData.get().getMannequin(minecraftServer.overworld(), "vault_guardian") != null) perk.type().trigger(perk.level(), handler.getPlayer());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((_, newPlayer, _) -> {
            for (ActivePerk perk : VaultData.get().getActivePerks()) if (NPCData.get().getMannequin(minecraftServer.overworld(), "vault_guardian") != null) perk.type().trigger(perk.level(), newPlayer);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            VaultData vault = VaultData.get();

            if (!vault.getActiveEvents().isEmpty()) {
                boolean removedAny = vault.getActiveEvents().removeIf(event -> event.tick(server.overworld()));
                if (removedAny) vault.setDirty();
            }
        });
    }

    protected LinkedHashSet<Map.Entry<UUID, Double>> getTopDonors() {
        return getLeaderboard().entrySet().stream().sorted(Map.Entry.<UUID, Double>comparingByValue().reversed()).limit(5)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}