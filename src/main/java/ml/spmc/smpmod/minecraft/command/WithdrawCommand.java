package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.spmc.smpmod.minecraft.economy.Economy;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class WithdrawCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("eco")
                .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.05)).executes(e -> {
                            double amount = DoubleArgumentType.getDouble(e, "amount");
                            return Economy.withdrawCommand(e, amount);
                        }));
    }
}
