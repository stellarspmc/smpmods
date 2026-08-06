package fun.spmc.smpmod.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import fun.spmc.smpmod.vault.entries.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class VaultData extends SavedData {
    public static final Codec<VaultData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VaultTier.CODEC.fieldOf("vault_tier").forGetter(VaultData::getCurrentTier),
            Codec.DOUBLE.fieldOf("current_money").forGetter(VaultData::getCurrentMoney),
            Codec.list(ActivePerk.CODEC).fieldOf("perks").forGetter(VaultData::getActivePerks),
            Codec.list(ConfiguredEvent.CODEC).fieldOf("events").forGetter(VaultData::getActiveEvents),
            UUIDUtil.CODEC.fieldOf("").forGetter(VaultData::getMannequinUuid)
    ).apply(instance, VaultData::new));

    public static final SavedDataType<VaultData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmods", "vault"),
            VaultData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private double currentMoney = 0;
    private VaultTier currentTier = VaultTier.ALPHA;
    private final List<ActivePerk> activePerks = new ArrayList<>();
    private final List<ConfiguredEvent> activeEvents = new ArrayList<>();
    private UUID mannequinUuid;

    public VaultData() {}

    public VaultData(VaultTier currentTier, double currentMoney, List<ActivePerk> perks, List<ConfiguredEvent> events, UUID mannequinUuid) {
        this.currentTier = currentTier;
        this.currentMoney = currentMoney;
        this.activePerks.addAll(perks);
        this.activeEvents.addAll(events);
        this.mannequinUuid = mannequinUuid;
    }

    public static VaultData get() {
        return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void addMoney(double amount) {
        this.currentMoney += amount;

        //while (canAdvance()) advance();
        setDirty();
    }

    private boolean canAdvance() {
        if (this.currentTier == VaultTier.GAMMA && getAvailableEvents().isEmpty() && getAvailablePerks().isEmpty()) return false;

        int totalTierItems = currentTier.getPerks().size() + currentTier.getEventPool().size();
        double stepCost = currentTier.getCostGoal() / totalTierItems;

        int expectedUnlocks = (int) Math.floor(this.currentMoney / stepCost);
        //int currentUnlocks = getActiveEventsForTier(currentTier) + getActivePerksForTier(currentTier);

        //return expectedUnlocks > currentUnlocks;
        return false;
    }

    private void advance() {
        ServerLevel level = minecraftServer.overworld();
        RandomSource random = level.getRandom();

        List<ConfiguredEvent> availableEvents = getAvailableEvents();
        List<ActivePerk> availablePerks = getAvailablePerks();

        // If the current tier is completely exhausted, move to the next tier
        if (availableEvents.isEmpty() && availablePerks.isEmpty()) {
            if (this.currentTier != VaultTier.GAMMA) {
                this.currentTier = this.currentTier.getNextTier();
                // Immediately check if we can unlock something in the new tier
                if (canAdvance()) advance();
            }
            return;
        }

        // Weighted random selection based on what's left
        boolean pickEvent = random.nextBoolean();
        if (pickEvent && availableEvents.isEmpty()) pickEvent = false;
        if (!pickEvent && availablePerks.isEmpty()) pickEvent = true;

        if (pickEvent) {
            ConfiguredEvent event = availableEvents.get(random.nextInt(availableEvents.size()));
            this.activeEvents.add(event);
            event.apply(level);
        } else {
            ActivePerk newPerk = availablePerks.get(random.nextInt(availablePerks.size()));

            // CLEANUP: Remove lower tier versions of this exact perk before adding the new one
            this.activePerks.removeIf(p -> p.type() == newPerk.type());
            this.activePerks.add(newPerk);

            newPerk.apply(level);
        }

        // Optional: Broadcast a message to the server that a Vault unlock happened!
    }

    // Helper methods for the logic above
    private List<ConfiguredEvent> getAvailableEvents() {
        return currentTier.getEventPool().stream().filter(e -> !this.activeEvents.contains(e)).toList();
    }

    private List<ActivePerk> getAvailablePerks() {
        // Only return perks we don't have, OR perks where our current level is lower
        return currentTier.getPerks().stream().filter(tierPerk -> {
            return this.activePerks.stream().noneMatch(activePerk ->
                    activePerk.type() == tierPerk.type() && activePerk.level() >= tierPerk.level());
        }).toList();
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

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            VaultData data = VaultData.get();
            for (ActivePerk perk : data.getActivePerks()) {
               //perk.type().trigger(perk.level(), handler.getPlayer());
            }
        });

        // Re-apply perks when a player dies and respawns
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            VaultData data = VaultData.get();
            for (ActivePerk perk : data.getActivePerks()) {
                //perk.type().trigger(perk.level(), newPlayer);
            }
        });
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
                if (amount > 0) isValid = true;
            } catch (NumberFormatException ignored) {
                // Input is not a valid number
            }

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
                vaultData.addMoney(finalAmount);
                player.sendSystemMessage(
                        Component.literal("Thank you! You donated ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" to the Vault!"))
                );

                this.close();
            });
        }
    }
}