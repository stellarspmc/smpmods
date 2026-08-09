package fun.spmc.smpmod.vault;

import eu.pb4.sgui.api.gui.AnvilInputGui;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.misc.NPCData;
import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.vault.entries.ActivePerk;
import fun.spmc.smpmod.vault.entries.ConfiguredEvent;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VaultUtils {
    public static float buffValue = 0f;

    public static Mannequin spawnVaultGuardian(ServerLevel level, BlockPos pos) {
        NPCData data = NPCData.get();
        if (!data.hasNpc("vault_guardian")) {
            Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED);
            if (mannequin == null) return null;
            mannequin.setPos(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(mannequin);
            mannequin.setProfile(NPCData.createCustomProfile("vault_guardian", new int[]{-2090072119,-764915421,-1802191614,-271883842}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmZkODNhY2NhOWFmM2JiYWQ3MDVmNzE0MzU1ZDk0MTA3NDEyY2E0ZWJiZDRjZTkzOTE2MGMxYmUxMGNjZDFhMiJ9fX0="));
            mannequin.setCustomName(Component.literal("Vault Guardian").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            mannequin.setImmovable(true);
            mannequin.setInvulnerable(true);
            mannequin.setHideDescription(true);

            data.registerNpc("vault_guardian", mannequin.getUUID());
            return mannequin;
        }
        return data.getMannequin(level, "vault_guardian");
    }
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("vault_guardian"))) {
                    new DonateAnvilGui((ServerPlayer) player).open();
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("vault_guardian"))) {
                    VaultData data = VaultData.get();

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
                }
            }
            return InteractionResult.PASS;
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
