package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class AllCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(BalanceCommand.buildCommand());
        dispatcher.register(DepositCommand.buildCommand());
        dispatcher.register(WithdrawCommand.buildCommand());
        dispatcher.register(DeployTestCommand.buildCommand());
        dispatcher.register(LockdownCommand.buildCommand());
        dispatcher.register(SurfaceCommand.buildCommand());
    }
}