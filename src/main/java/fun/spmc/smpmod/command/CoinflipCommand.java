package fun.spmc.smpmod.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class CoinflipCommand {
    private static final HashMap<UUID, Long> playerTimeMap = new HashMap<>();

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("mapart")
                .then(Commands.argument("cash", DoubleArgumentType.doubleArg(0, 10000))
                        .executes(ctx -> processCoinflip(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "cash"))));
    }

    private static int processCoinflip(CommandSourceStack source, double cash) throws CommandSyntaxException {
        if (!source.isPlayer()) return 0;
        EconomyData eco = EconomyData.get();
        ServerPlayer player = source.getPlayerOrException();
        if (playerTimeMap.get(player.getUUID()) == null || playerTimeMap.getOrDefault(player.getUUID(), 0L) + 3600000L >= System.currentTimeMillis()) {
            if (eco.getBalance(player.getUUID()) > cash) {
                boolean random = player.getRandom().nextFloat() >= .65;
                if (random) {
                    eco.changeBalance(player.getUUID(), cash);
                    MessageUtils.sendSuccessMessage(player, String.format("Nice! You got $%.2f from a coinflip!", cash));
                }
                else {
                    eco.changeBalance(player.getUUID(), -cash);
                    MessageUtils.sendErrorMessage(player, String.format("Oh no! You lost $%.2f from a coinflip!", cash));
                }
                if (playerTimeMap.get(player.getUUID()) == null) playerTimeMap.put(player.getUUID(), System.currentTimeMillis());
                else playerTimeMap.replace(player.getUUID(), System.currentTimeMillis());
                return 1;
            }
        }

        long duration = System.currentTimeMillis() - playerTimeMap.get(player.getUUID());
        MessageUtils.sendErrorMessage(player, String.format("You still have %02d:%02d until your next coinflip!", (duration / (1000 * 60)) % 60, (duration / 1000) % 60));
        return 0;
    }
}
