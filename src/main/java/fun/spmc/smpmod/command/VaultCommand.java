package fun.spmc.smpmod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.vault.VaultData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.phys.Vec3;

public class VaultCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildVault() {
        return Commands.literal("vault")
                .executes(VaultCommand::getVaultStatus)
                .then(Commands.literal("setup")
                        .requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.ADMINS))
                        .executes(VaultCommand::setupVault)
                );
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

        VaultData vault = VaultData.get();
        if (vault.getMannequinUuid() != null) {
            MessageUtils.sendErrorMessage(player, "Vault has not been initialized yet!");
            return 0;
        }

        player.sendSystemMessage(Component.literal("The Vault is").withStyle(ChatFormatting.GOLD)
                .append(vault.getCurrentMoney() + "/"));
        return 1;
    }
}
