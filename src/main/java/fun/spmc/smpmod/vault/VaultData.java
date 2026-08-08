package fun.spmc.smpmod.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.vault.entries.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class VaultData extends SavedData {
    public static final Codec<VaultData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VaultTier.CODEC.fieldOf("vault_tier").forGetter(VaultData::getCurrentTier),
            Codec.DOUBLE.fieldOf("current_money").forGetter(VaultData::getCurrentMoney),
            Codec.list(ActivePerk.CODEC).fieldOf("perks").forGetter(VaultData::getActivePerks),
            Codec.list(ConfiguredEvent.CODEC).fieldOf("events").forGetter(VaultData::getActiveEvents),
            UUIDUtil.CODEC.fieldOf("mannequin_uuid").forGetter(VaultData::getMannequinUuid),
            Codec.unboundedMap(UUIDUtil.CODEC, Codec.DOUBLE).optionalFieldOf("leaderboard", Map.of()).forGetter(VaultData::getLeaderboard)
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
    public UUID mannequinUuid;
    private final Map<UUID, Double> leaderboard = new HashMap<>();

    public VaultData() {}

    public VaultData(VaultTier currentTier, double currentMoney, List<ActivePerk> perks, List<ConfiguredEvent> events, UUID mannequinUuid, Map<UUID, Double> leaderboard) {
        this.currentTier = currentTier;
        this.currentMoney = currentMoney;
        this.activePerks.addAll(perks);
        this.activeEvents.addAll(events);
        this.mannequinUuid = mannequinUuid;
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
            currentMoney = Math.min(0, currentMoney - currentTier.getCostGoal());
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
    public UUID getMannequinUuid() { return mannequinUuid; }
    public List<ActivePerk> getActivePerks() { return activePerks; }
    public List<ConfiguredEvent> getActiveEvents() { return activeEvents; }

    public Mannequin getOrCreateMannequin(ServerLevel level, Vec3 pos) {
        if (this.mannequinUuid != null) {
            Entity entity = level.getEntity(this.mannequinUuid);
            if (entity instanceof Mannequin mannequin && mannequin.isAlive()) return mannequin;
            return null;
        }

        Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.COMMAND);
        if (mannequin != null) {
            mannequin.setPos(pos.x, pos.y, pos.z);
            level.addFreshEntity(mannequin);
            mannequin.setProfile(ResolvableProfile.createUnresolved("spmc"));
            mannequin.setCustomName(Component.literal("Vault Guardian").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            mannequin.setImmovable(true);
            mannequin.setInvulnerable(true);
            mannequin.setHideDescription(true);
            this.mannequinUuid = mannequin.getUUID();
            setDirty();
        }

        return mannequin;
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                VaultData data = VaultData.get();
                if (data.getMannequinUuid() != null && mannequin.getUUID().equals(data.getMannequinUuid())) {
                    player.sendSystemMessage(Component.literal("Vault Status").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("Tier: ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(data.getCurrentTier().name()).withStyle(ChatFormatting.GREEN)));
                    player.sendSystemMessage(Component.literal("Balance: ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(String.format("$%.2f / $%.2f", data.getCurrentMoney(), data.getCurrentTier().getCostGoal())).withStyle(ChatFormatting.AQUA)));

                    player.sendSystemMessage(Component.literal("Top Donors:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    List<Map.Entry<UUID, Double>> topDonors = data.getTopDonors().stream().toList();
                    if (topDonors.isEmpty()) player.sendSystemMessage(Component.literal("  - No donations yet").withStyle(ChatFormatting.GRAY));
                    else {
                        int rank = 1;
                        for (Map.Entry<UUID, Double> entry : topDonors) {
                            player.sendSystemMessage(Component.literal(String.format("  #%d %s: ", rank++, EconomyData.get().resolveName(entry.getKey())))
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal(String.format("$%.2f", entry.getValue())).withStyle(ChatFormatting.GREEN)));
                        }
                    }

                    player.sendSystemMessage(Component.literal("Active Perks:").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                    if (data.getActivePerks().isEmpty()) {
                        player.sendSystemMessage(Component.literal("  - None").withStyle(ChatFormatting.GRAY));
                    } else {
                        for (ActivePerk perk : data.getActivePerks()) {
                            player.sendSystemMessage(Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY)
                                    .append(Component.literal(perk.toString()).withStyle(ChatFormatting.GRAY)));
                        }
                    }
                    player.sendSystemMessage(Component.literal("Active Events:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    if (data.getActiveEvents().isEmpty()) {
                        player.sendSystemMessage(Component.literal("  - None").withStyle(ChatFormatting.GRAY));
                    } else {
                        for (ConfiguredEvent event : data.getActiveEvents()) {
                            player.sendSystemMessage(Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY)
                                    .append(Component.literal(event.toString()).withStyle(ChatFormatting.GRAY)));
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                VaultData data = VaultData.get();
                if (data.getMannequinUuid() != null && mannequin.getUUID().equals(data.getMannequinUuid())) {
                    DonateAnvilGui gui = new DonateAnvilGui((ServerPlayer) player);
                    gui.open();
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            for (ActivePerk perk : VaultData.get().getActivePerks()) if (get().getMannequinUuid() != null) perk.type().trigger(perk.level(), handler.getPlayer());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((_, newPlayer, _) -> {
            for (ActivePerk perk : VaultData.get().getActivePerks()) {
                if (get().getMannequinUuid() != null) perk.type().trigger(perk.level(), newPlayer);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            VaultData vault = VaultData.get();

            if (vault.getMannequinUuid() != null) {
                ServerLevel level = server.overworld();
                Entity entity = level.getEntity(vault.getMannequinUuid());

                if (entity instanceof Mannequin mannequin && mannequin.isAlive()) {
                    Player nearestPlayer = level.getNearestPlayer(mannequin, 12.0);

                    if (nearestPlayer != null) {
                        mannequin.lookAt(EntityAnchorArgument.Anchor.EYES,
                                nearestPlayer.position().add(0, nearestPlayer.getEyeHeight(), 0));
                    }
                }
            }

            if (!vault.getActiveEvents().isEmpty()) {
                boolean removedAny = vault.getActiveEvents().removeIf(event -> event.tick(server.overworld()));
                if (removedAny) vault.setDirty();
            }
        });
    }

    private LinkedHashSet<Map.Entry<UUID, Double>> getTopDonors() {
        return getLeaderboard().entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(5)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static class DonateAnvilGui extends AnvilInputGui {

        public DonateAnvilGui(ServerPlayer player) {
            super(player, false);

            this.setTitle(Component.literal("Vault Donation"));
            this.setDefaultInputValue("10");
            this.updateOutputSlot();
        }

        @Override
        public void onInput(String input) {
            this.updateOutputSlot();
        }

        private void updateOutputSlot() {
            String input = getInput().trim();
            double amount = 0;
            boolean isValid = false;

            try {
                amount = Double.parseDouble(input);
                if (amount > 0 && EconomyData.get().getBalance(player.getUUID()) >= amount) isValid = true;
            } catch (NumberFormatException ignored) {}

            ItemStack outputItem;
            if (isValid) {
                outputItem = Items.STAINED_GLASS_PANE.lime().getDefaultInstance();
                outputItem.set(DataComponents.CUSTOM_NAME,
                        Component.literal("Click to Donate $" + String.format("%.2f", amount))
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            } else {
                outputItem = Items.STAINED_GLASS_PANE.red().getDefaultInstance();
                outputItem.set(DataComponents.CUSTOM_NAME,
                        Component.literal("Enter a valid amount (> 0)")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }

            final double finalAmount = amount;
            final boolean canDonate = isValid;

            this.setSlot(2, outputItem, (_, _, _, _) -> {
                if (!canDonate) return;

                VaultData vaultData = VaultData.get();
                if (EconomyData.get().changeBalance(player.getUUID(), -finalAmount)) {
                    vaultData.recordDonation(player.getUUID(), finalAmount);
                    player.sendSystemMessage(
                            Component.literal("Thank you! You donated ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.GOLD))
                                    .append(Component.literal(" to the Vault!"))
                    );
                } else MessageUtils.sendErrorMessage(player, "You do not have enough money to donate to the Vault.");
                this.close();
            });
        }
    }
}