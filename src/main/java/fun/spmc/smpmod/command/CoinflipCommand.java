package fun.spmc.smpmod.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.economy.EconomySavedData;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CoinflipCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("mapart")
                .then(Commands.argument("cash", DoubleArgumentType.doubleArg(0, 10000))
                        .executes(ctx -> processCoinflip(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "cash"))));
    }

    private static int processCoinflip(CommandSourceStack source, double cash) throws CommandSyntaxException {
        if (!source.isPlayer()) return 0;
        EconomySavedData eco = EconomySavedData.get();
        ServerPlayer player = source.getPlayerOrException();
        if (eco.getBalance(player.getUUID()) > cash) {
            boolean random = player.getRandom().nextFloat() >= .6;
            if (random) {
                eco.changeBalance(player.getUUID(), cash);
                MessageUtils.sendSuccessMessage(player, String.format("Nice! You got $%.2f from a coinflip!", cash));
            }
            else {
                eco.changeBalance(player.getUUID(), -cash);
                MessageUtils.sendErrorMessage(player, String.format("Oh no! You lost $%.2f from a coinflip!", cash));
            }
        }
        return 1;
    }
}
