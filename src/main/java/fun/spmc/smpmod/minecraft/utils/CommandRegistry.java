package fun.spmc.smpmod.minecraft.utils;

import fun.spmc.smpmod.minecraft.command.*;
import fun.spmc.smpmod.minecraft.command.EconomyCommands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class CommandRegistry {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(SurfaceCommand.buildCommand());
        dispatcher.register(EnderChestCommand.buildCommand());

        dispatcher.register(EconomyCommands.buildBalance());
        dispatcher.register(EconomyCommands.buildDeposit());
        dispatcher.register(EconomyCommands.buildWithdraw());
        dispatcher.register(EconomyCommands.buildSend());
        dispatcher.register(EconomyCommands.buildTop());
    }
}