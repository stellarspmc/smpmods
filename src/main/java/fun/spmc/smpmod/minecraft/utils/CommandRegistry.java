package fun.spmc.smpmod.minecraft.utils;

import fun.spmc.smpmod.minecraft.command.*;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class CommandRegistry {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(SurfaceCommand.buildCommand());
        dispatcher.register(EnderChestCommand.buildCommand());
    }
}