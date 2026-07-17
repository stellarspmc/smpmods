package fun.spmc.smpmod.minecraft.utils;

import fun.spmc.smpmod.minecraft.command.*;
import fun.spmc.smpmod.minecraft.command.EconomyCommands;

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
    }
}