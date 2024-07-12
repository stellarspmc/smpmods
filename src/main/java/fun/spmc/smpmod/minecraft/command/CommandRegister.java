package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class CommandRegister {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(SurfaceCommand.buildCommand());
    }
}