package fun.spmc.smpmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandRegistry {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection ignoredCommandSelection) {
        dispatcher.register(SurfaceCommand.buildCommand());
        dispatcher.register(EnderChestCommand.buildCommand());

        dispatcher.register(EconomyCommands.buildBalance());
        dispatcher.register(EconomyCommands.buildDeposit());
        dispatcher.register(EconomyCommands.buildWithdraw(context));
        dispatcher.register(EconomyCommands.buildSend());
        //dispatcher.register(EconomyCommands.buildATM());
        dispatcher.register(EconomyCommands.buildMarket(context));
        dispatcher.register(EconomyCommands.buildTop());
        dispatcher.register(EconomyCommands.buildBalanceAlias());

        dispatcher.register(VaultCommand.buildVault());
        dispatcher.register(CoinflipCommand.buildCommand());
        dispatcher.register(MapArtCommand.buildCommand());
        dispatcher.register(FishingCommand.buildCommand());
    }
}