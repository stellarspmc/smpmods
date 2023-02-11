package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.spmc.smpmod.minecraft.economy.Economy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DepositCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("deposit").executes(Economy::depositCommand);
    }
}
