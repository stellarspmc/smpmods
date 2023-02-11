package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ml.spmc.smpmod.utils.UtilClass;
import com.mojang.brigadier.arguments.StringArgumentType;
import ml.spmc.smpmod.utils.sql.DatabaseManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class BalanceCommand {
        public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
            return Commands.literal("balance")
                    .then(
                            Commands.argument("playerName", StringArgumentType.string())
                                    .executes(e -> {
                                        String string = StringArgumentType.getString(e, "playerName");
                                        return balanceCommand(e, string);
                                    })
                    )
                    .then(
                            Commands.argument("player", EntityArgument.player())
                                    .executes(e -> {
                                        String player = EntityArgument.getPlayer(e, "player").getName().getString();
                                        return balanceCommand(e, player);
                                    })
                    )
                    .executes(e -> balanceCommand(e, e.getSource().getPlayerOrException().getName().getString()));
        }

        public static int balanceCommand(CommandContext<CommandSourceStack> ctx, String player) {
            DatabaseManager dm = UtilClass.getDatabaseManager();
            double bal = dm.getBalance(player);
            ctx.getSource().sendSuccess(Component.literal(player + " has $" + bal), false);
            return 1;
        }
}
