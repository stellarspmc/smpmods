package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import ml.spmc.smpmod.minecraft.command.admin.DeployCommand;
import ml.spmc.smpmod.minecraft.command.admin.TPAllCommand;
import ml.spmc.smpmod.minecraft.command.screen.command.ECCommand;
import net.minecraft.server.command.ServerCommandSource;

public class AllCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(BalanceCommand.buildCommand());
        //dispatcher.register(WithdrawCommand.buildCommand());
        dispatcher.register(DeployCommand.buildCommand());
        dispatcher.register(LockdownCommand.buildCommand());
        dispatcher.register(SurfaceCommand.buildCommand());
        dispatcher.register(SurfaceCommand.buildAlise());
        dispatcher.register(DiscordCommand.buildCommand());
        dispatcher.register(StatsCommand.buildCommand());
        dispatcher.register(MyIPCommand.buildCommand());
        dispatcher.register(ECCommand.buildCommand());
        dispatcher.register(TPAllCommand.buildCommand());
    }
}