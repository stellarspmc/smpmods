package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import ml.spmc.smpmod.minecraft.command.screen.command.ECCommand;
import ml.spmc.smpmod.minecraft.command.screen.command.GarbageCommand;
import net.minecraft.server.command.ServerCommandSource;

public class AllCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        //dispatcher.register(BalanceCommand.buildCommand());
        //dispatcher.register(WithdrawCommand.buildCommand());

        //dispatcher.register(DeployCommand.buildCommand());
        dispatcher.register(SurfaceCommand.buildCommand());
        //dispatcher.register(MyIPCommand.buildCommand());
        dispatcher.register(ECCommand.buildCommand());
        dispatcher.register(GarbageCommand.buildCommand());
    }
}