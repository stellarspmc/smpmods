package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.spmc.smpmod.minecraft.economy.Economy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class WithdrawCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("eco")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.05)).executes(e -> {
                            double amount = DoubleArgumentType.getDouble(e, "amount");
                            return Economy.withdrawCommand(e, amount);
                        }));
    }
}
