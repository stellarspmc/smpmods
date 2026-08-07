package fun.spmc.smpmod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.vault.VaultData;
import fun.spmc.smpmod.vault.entries.ActivePerk;
import fun.spmc.smpmod.vault.entries.ConfiguredEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class VaultCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildVault() {
        return Commands.literal("vault")
                .executes(VaultCommand::getVaultStatus)
                .then(Commands.literal("setup")
                        .requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS))
                        .executes(VaultCommand::setupVault)
                ).then(Commands.literal("kill")
                        .requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS))
                        .executes(VaultCommand::killVault));
    }

    private static int killVault(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();

        VaultData vault = VaultData.get();
        if (vault.getMannequinUuid() != null) {
            Entity entity = level.getEntity(vault.getMannequinUuid());
            if (entity != null) {
                entity.discard();
                vault.mannequinUuid = null;
                MessageUtils.sendSuccessMessage(ctx.getSource().getPlayerOrException(), "Vault mannequin killed!");
                return 1;
            }

        }
        MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Vault mannequin isn't alive!");
        return 0;
    }

    private static int setupVault(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        Vec3 pos = ctx.getSource().getPosition();

        VaultData vault = VaultData.get();
        if (vault.getMannequinUuid() != null) {
            MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Vault mannequin already exists!");
            return 0;
        }

        if (vault.getOrCreateMannequin(level, pos) == null) {
            MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Vault mannequin already exists!");
            return 0;
        }

        MessageUtils.sendSuccessMessage(ctx.getSource().getPlayerOrException(), "Vault mannequin created successfully!");
        return 1;
    }

    private static int getVaultStatus(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        VaultData data = VaultData.get();
        if (data.getMannequinUuid() != null) {
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
            return 1;
        }

        MessageUtils.sendErrorMessage(player, "Vault has not been initialized yet!");
        return 0;
    }
}
