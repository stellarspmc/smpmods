package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import ml.spmc.smpmod.minecraft.command.screen.command.*;

public class CommandRegister {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(BalanceCommand.buildCommand());
        dispatcher.register(SurfaceCommand.buildCommand());
        dispatcher.register(ECCommand.buildCommand());
        dispatcher.register(GarbageCommand.buildCommand());
    }
}