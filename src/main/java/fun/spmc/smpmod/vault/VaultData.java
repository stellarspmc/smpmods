package fun.spmc.smpmod.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class VaultData extends SavedData {
    public static final Codec<VaultData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VaultTier.CODEC.fieldOf("vault_tier").forGetter(VaultData::getCurrentTier),
            Codec.DOUBLE.fieldOf("current_money").forGetter(VaultData::getCurrentMoney),
            Codec.list(VaultTier.ActivePerk.CODEC).fieldOf("perks").forGetter(VaultData::getActivePerks),
            Codec.list(VaultTier.ConfiguredEvent.CODEC).fieldOf("events").forGetter(VaultData::getActiveEvents)
    ).apply(instance, VaultData::new));

    public static final SavedDataType<VaultData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmods", "vault"),
            VaultData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private double currentMoney = 0;
    private VaultTier currentTier = VaultTier.ALPHA;
    private final List<VaultTier.ActivePerk> activePerks = new ArrayList<>();
    private final List<VaultTier.ConfiguredEvent> activeEvents = new ArrayList<>();

    public VaultData() {}

    public VaultData(VaultTier currentTier, double currentMoney, List<VaultTier.ActivePerk> perks, List<VaultTier.ConfiguredEvent> events) {
        this.currentTier = currentTier;
        this.currentMoney = currentMoney;
        this.activePerks.addAll(perks);
        this.activeEvents.addAll(events);
    }

    public static VaultData get() {
        return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void addMoney(double amount) {
        this.currentMoney += amount;
        if (this.currentMoney >= this.currentTier.getCostGoal() && this.currentTier != VaultTier.GAMMA) advance();

        setDirty();
    }

    private void advance() {
        ServerLevel level = minecraftServer.overworld();
        boolean isEvent = level.getRandom().nextFloat() > 0.5f;
        VaultEntry entry;

        if (getCurrentTier().getEventPool().stream().filter((a) -> !getActiveEvents().contains(a)).collect(Collectors.toCollection(ArrayList::new)).isEmpty() && getCurrentTier().getPerks().stream().filter((a) -> !getActivePerks().contains(a)).collect(Collectors.toCollection(ArrayList::new)).isEmpty()) {
            advanceToNextTier();
            return;
        }

        if (isEvent) {
            ArrayList<VaultTier.ConfiguredEvent> events = getCurrentTier().getEventPool().stream().filter((a) -> !getActiveEvents().contains(a)).collect(Collectors.toCollection(ArrayList::new));
            entry = events.get(level.getRandom().nextInt(events.size()));
        } else {
            ArrayList<VaultTier.ActivePerk> perks = getCurrentTier().getPerks().stream().filter((a) -> !getActivePerks().contains(a)).collect(Collectors.toCollection(ArrayList::new));
            entry = perks.get(level.getRandom().nextInt(perks.size()));
        }

        entry.apply(level);
        setDirty();
    }

    private void advanceToNextTier() {
        this.currentTier = this.currentTier.getNextTier();
    }

    // Getters
    public double getCurrentMoney() { return currentMoney; }
    public VaultTier getCurrentTier() { return currentTier; }
    public List<VaultTier.ActivePerk> getActivePerks() { return activePerks; }
    public List<VaultTier.ConfiguredEvent> getActiveEvents() { return activeEvents; }
}