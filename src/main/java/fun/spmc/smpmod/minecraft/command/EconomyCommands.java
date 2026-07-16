package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.minecraft.economy.EconomyConfig;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class EconomyCommands {

    // --- /balance ---
    public static LiteralArgumentBuilder<CommandSourceStack> buildBalance() {
        return Commands.literal("balance")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> balanceCommand(ctx, EntityArgument.getPlayer(ctx, "player"))))
                .executes(ctx -> balanceCommand(ctx, ctx.getSource().getPlayerOrException()));
    }

    private static int balanceCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        EconomySavedData eco = EconomySavedData.get(ctx.getSource().getLevel());
        double bal = eco.getBalance(target.getUUID());
        boolean isSelf = ctx.getSource().getEntity() == target;

        ctx.getSource().sendSuccess(() -> Component.literal("ECO: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal((isSelf ? "You" : target.getName().getString()) + " have ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", bal)).withStyle(ChatFormatting.RED))
                .append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
        return 1;
    }

    // --- /deposit ---
    public static LiteralArgumentBuilder<CommandSourceStack> buildDeposit() {
        return Commands.literal("deposit")
                .executes(EconomyCommands::depositHand)
                .then(Commands.literal("all").executes(EconomyCommands::depositAll));
    }

    private static int depositHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack hand = player.getInventory().getSelectedItem();
        double itemValue = EconomyConfig.getItemValue(hand.getItem());

        if (hand.isEmpty() || itemValue <= 0) {
            ctx.getSource().sendFailure(Component.literal("ERR: Hold a valid currency item or use /deposit all.").withStyle(ChatFormatting.DARK_RED));
            return -1;
        }

        double totalValue = hand.getCount() * itemValue;
        EconomySavedData eco = EconomySavedData.get(player.level());

        if (eco.changeBalance(player.getUUID(), totalValue)) {
            player.getInventory().removeFromSelected(true); // Remove hand stack
            ctx.getSource().sendSuccess(() -> Component.literal("ECO: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("Added ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.format("$%.2f", totalValue)).withStyle(ChatFormatting.RED))
                    .append(Component.literal(" to your account.").withStyle(ChatFormatting.GOLD)), false);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("ERR: Max balance reached.").withStyle(ChatFormatting.DARK_RED));
        return -1;
    }

    private static int depositAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EconomySavedData eco = EconomySavedData.get(player.level());
        double totalDeposited = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            double itemValue = EconomyConfig.getItemValue(stack.getItem());

            if (itemValue > 0 && !stack.isEmpty()) {
                double stackValue = stack.getCount() * itemValue;
                totalDeposited += stackValue;
                player.getInventory().removeItem(i, stack.getCount());
            }
        }

        if (totalDeposited > 0) {
            eco.changeBalance(player.getUUID(), totalDeposited);
            double finalTotal = totalDeposited;
            ctx.getSource().sendSuccess(() -> Component.literal("ECO: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("Deposited ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.format("$%.2f", finalTotal)).withStyle(ChatFormatting.RED))
                    .append(Component.literal(" to your account.").withStyle(ChatFormatting.GOLD)), false);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("ERR: No valid currency items found in inventory.").withStyle(ChatFormatting.DARK_RED));
        return -1;
    }

    // --- /withdraw ---
    public static LiteralArgumentBuilder<CommandSourceStack> buildWithdraw() {
        return Commands.literal("withdraw")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.10))
                        .executes(ctx -> {
                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                            return withdrawCommand(ctx, amount);
                        }));
    }

    private static int withdrawCommand(CommandContext<CommandSourceStack> ctx, double amount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EconomySavedData eco = EconomySavedData.get(player.level());

        amount = Math.round(amount * 100.0) / 100.0;

        if (eco.changeBalance(player.getUUID(), -amount)) {
            double remainingUnconverted = dropItem(amount, player);

            if (remainingUnconverted > 0) eco.changeBalance(player.getUUID(), remainingUnconverted);

            double withdrawnAmount = amount - remainingUnconverted;
            ctx.getSource().sendSuccess(() -> Component.literal("ECO: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("You withdrew ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.format("$%.2f", withdrawnAmount)).withStyle(ChatFormatting.RED))
                    .append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("ERR: Insufficient balance to withdraw ").withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.GOLD)));
            return -1;
        }
    }

    public static double dropItem(double amount, ServerPlayer player) {
        amount = Math.round(amount * 100.0) / 100.0;

        for (Map.Entry<Item, Double> entry : EconomyConfig.getSortedCurrencyValues().entrySet()) {
            Item item = entry.getKey();
            double val = entry.getValue();

            if (val <= 0) continue;

            int countToGive = (int) (amount / val);
            if (countToGive > 0) {
                amount -= countToGive * val;
                amount = Math.round(amount * 100.0) / 100.0;
                int maxStack = item.getDefaultMaxStackSize();
                while (countToGive > 0) {
                    int stackSize = Math.min(countToGive, maxStack);
                    ItemStack stack = new ItemStack(item, stackSize);
                    if (!player.getInventory().add(stack)) {
                        ItemEntity itemEntity = player.drop(stack, false);
                        if (itemEntity != null) itemEntity.setNoPickUpDelay();
                    }
                    countToGive -= stackSize;
                }
            }
        }
        return amount;
    }

    // --- /send ---
    public static LiteralArgumentBuilder<CommandSourceStack> buildSend() {
        return Commands.literal("send")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.10))
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                    amount = Math.round(amount * 100.0) / 100.0;

                                    EconomySavedData eco = EconomySavedData.get(sender.level());
                                    if (eco.changeBalance(sender.getUUID(), -amount)) {
                                        eco.changeBalance(target.getUUID(), amount);

                                        double finalAmount = amount;
                                        target.sendSystemMessage(Component.literal("ECO: ").withStyle(ChatFormatting.GREEN)
                                                .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.RED))
                                                .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(sender.getName().getString()).withStyle(ChatFormatting.RED)));

                                        ctx.getSource().sendSuccess(() -> Component.literal("ECO: ").withStyle(ChatFormatting.GREEN)
                                                .append(Component.literal("You sent ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.RED))
                                                .append(Component.literal(" to ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(target.getName().getString()).withStyle(ChatFormatting.RED)), false);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("ERR: Insufficient funds.").withStyle(ChatFormatting.DARK_RED));
                                        return -1;
                                    }
                                })));
    }

    // --- /top ---
    public static LiteralArgumentBuilder<CommandSourceStack> buildTop() {
        return Commands.literal("top")
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> topCommand(ctx, IntegerArgumentType.getInteger(ctx, "page"))))
                .executes(ctx -> topCommand(ctx, 1));
    }

    private static int topCommand(CommandContext<CommandSourceStack> ctx, int page) {
        EconomySavedData eco = EconomySavedData.get(ctx.getSource().getLevel());
        String output = eco.top(page);
        ctx.getSource().sendSuccess(() -> Component.literal(" ----").withStyle(ChatFormatting.YELLOW)
                .append(" Economy Top ").withStyle(ChatFormatting.GOLD)
                .append("----").withStyle(ChatFormatting.YELLOW)
                .append("\n" + output), false);
        return 1;
    }
}