package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ml.spmc.smpmod.utils.UtilClass;
import ml.spmc.smpmod.utils.sql.DatabaseManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class BalanceCommand {
        public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
            return CommandManager.literal("balance")
                    .then(
                            CommandManager.argument("playerName", StringArgumentType.string())
                                    .executes(e -> {
                                        String string = StringArgumentType.getString(e, "playerName");
                                        return balanceCommand(e, string);
                                    })
                    )
                    .then(
                            CommandManager.argument("player", EntityArgumentType.players())
                                    .executes(e -> {
                                        String player = EntityArgumentType.getPlayer(e, "player").getName().getString();
                                        return balanceCommand(e, player);
                                    })
                    )
                    .executes(e -> balanceCommand(e, e.getSource().getPlayerOrThrow().getName().getString()));
        }

        public static int balanceCommand(CommandContext<ServerCommandSource> ctx, String player) {
            DatabaseManager dm = UtilClass.getDatabaseManager();
            double bal = dm.getBalance(player);
            ctx.getSource().sendFeedback(Text.literal(player + " has $" + bal), false);
            return 1;
        }
}
