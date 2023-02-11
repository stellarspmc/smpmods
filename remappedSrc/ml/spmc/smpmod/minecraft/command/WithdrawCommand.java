package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.spmc.smpmod.minecraft.economy.Economy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class WithdrawCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("withdraw")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(e -> {
                            int amount = IntegerArgumentType.getInteger(e, "amount");
                            return Economy.withdrawCommand(e, amount);
                        })).then(Commands.argument("type", StringArgumentType.string()).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(e -> {
                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                    String type = StringArgumentType.getString(e, "type");
                                    return Economy.withdrawSpec(e, type, amount);
                                })));
    }
}
