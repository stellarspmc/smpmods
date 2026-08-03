package fun.spmc.smpmod.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.economy.EconomySavedData;
import fun.spmc.smpmod.economy.atm.ATMMenu;
import fun.spmc.smpmod.economy.fluctuate.FluctuationData;
import fun.spmc.smpmod.economy.fluctuate.MarketState;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EconomyCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> buildBalance() {
        return Commands.literal("bal")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> balanceCommand(ctx, EntityArgument.getPlayer(ctx, "player"))))
                .executes(ctx -> balanceCommand(ctx, ctx.getSource().getPlayerOrException()));
    }

    private static int balanceCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        EconomySavedData eco = EconomySavedData.get(ctx.getSource().getLevel());
        double bal = eco.getBalance(target.getUUID());
        boolean isSelf = ctx.getSource().getEntity() == target;

        ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal((isSelf ? "You" : target.getName().getString()) + " have ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", bal)).withStyle(ChatFormatting.RED))
                .append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildDeposit() {
        return Commands.literal("deposit")
                .executes(EconomyCommands::depositHand)
                .then(Commands.literal("all").executes(EconomyCommands::depositAll));
    }

    private static int depositHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack hand = player.getInventory().getSelectedItem();

        if (hand.isEmpty()) {
            MessageUtils.sendErrorMessage(player, "Hold a valid market item or use /deposit all.");
            return -1;
        }

        double payout = processItemDeposit(player, hand);
        if (payout <= 0) {
            MessageUtils.sendErrorMessage(player, "This item cannot be deposited into the market.");
            return -1;
        }

        hand.setCount(0);
        MessageUtils.sendSuccessMessage(player, String.format("Deposited items for $%.2f to your account.", payout));
        return 1;
    }

    private static int depositAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        double totalPayout = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            double payout = processItemDeposit(player, stack);
            if (payout > 0) {
                totalPayout += payout;
                player.getInventory().removeItem(i, stack.getCount());
            }
        }

        if (totalPayout > 0) {
            MessageUtils.sendSuccessMessage(player, String.format("Deposited all valid items for $%.2f to your account.", totalPayout));
            return 1;
        }

        MessageUtils.sendErrorMessage(player, "No valid market currency items found in inventory.");
        return -1;
    }

    private static double processItemDeposit(ServerPlayer player, ItemStack stack) {
        Item baseItem = unwrapBlockToItem(stack.getItem());
        int multiplier = (baseItem != stack.getItem()) ? 9 : 1;
        int totalUnits = stack.getCount() * multiplier;
        double blockTax = (multiplier == 9) ? 0.93 : 1.0;
        return MarketState.sellMineral(player, baseItem, totalUnits, blockTax);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildWithdraw(CommandBuildContext buildContext) {
        return Commands.literal("withdraw")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(100))
                        .executes(ctx -> withdrawCashAmount(ctx, DoubleArgumentType.getDouble(ctx, "amount"))))
                .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .suggests((_, builder) -> SharedSuggestionProvider.suggestResource(
                                MarketState.getState().getAll().keySet().stream().map(BuiltInRegistries.ITEM::getKey),
                                builder
                        ))
                        .executes(ctx -> handleWithdrawItem(ctx, 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 6400))
                                .executes(ctx -> handleWithdrawItem(ctx, IntegerArgumentType.getInteger(ctx, "count")))));
    }

    private static int withdrawCashAmount(CommandContext<CommandSourceStack> ctx, double rawAmount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        int diamondCount = (int) (rawAmount / 100.0);
        if (diamondCount <= 0) {
            MessageUtils.sendErrorMessage(player, "Minimum cash withdrawal is $100.00 (1x Diamond).");
            return -1;
        }

        double totalCost = MarketState.buyMineral(player, Items.DIAMOND, diamondCount);
        if (totalCost == -1) {
            MessageUtils.sendErrorMessage(player, String.format("Insufficient balance for %dx Diamonds.", diamondCount));
            return -1;
        }

        giveExactItems(player, Items.DIAMOND, diamondCount);
        MessageUtils.sendSuccessMessage(player, String.format("Withdrew %dx Diamonds for $%.2f.", diamondCount, totalCost));
        return 1;
    }

    private static int handleWithdrawItem(CommandContext<CommandSourceStack> ctx, int count) throws CommandSyntaxException {
        Item item = ItemArgument.getItem(ctx, "item").item().value();
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        double totalCost = MarketState.buyMineral(player, item, count);

        if (totalCost == -2) {
            MessageUtils.sendErrorMessage(player, Component.translatable(item.getDescriptionId()) + " is not a tradeable market item.");
            return -1;
        }
        if (totalCost == -1) {
            MarketState market = MarketState.getState();
            FluctuationData data = market.get(item);
            double estimatedCost = data != null ? data.getBulkBuyCost(count) : 0;
            MessageUtils.sendErrorMessage(player, String.format("Insufficient balance. You need $%.2f to withdraw %dx %s.",
                    estimatedCost, count, Component.translatable(item.getDescriptionId())));
            return -1;
        }

        giveExactItems(player, item, count);
        MessageUtils.sendSuccessMessage(player, String.format("Withdrew %dx %s for $%.2f.", count, Component.translatable(item.getDescriptionId()), totalCost));
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildSend() {
        return Commands.literal("send")
                .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.10))
                .executes((ctx -> {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                    double amount = Math.round(DoubleArgumentType.getDouble(ctx, "amount") * 100.0) / 100.0;

                    if (sender.getUUID().equals(target.getUUID())) {
                        MessageUtils.sendErrorMessage(sender, "You cannot send money to yourself.");
                        return -1;
                    }

                    EconomySavedData eco = EconomySavedData.get(sender.level());
                    if (eco.changeBalance(sender.getUUID(), -amount)) {
                        eco.changeBalance(target.getUUID(), amount);

                        target.sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                                .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.RED))
                                .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(sender.getName().getString()).withStyle(ChatFormatting.RED)));

                        MessageUtils.sendSuccessMessage(sender, String.format("Sent $%.2f to %s.", amount, target.getName().getString()));
                        return 1;
                    }

                    MessageUtils.sendErrorMessage(sender, "Insufficient funds.");
                    return -1;
        }))));
    }


    public static LiteralArgumentBuilder<CommandSourceStack> buildATM() {
        return Commands.literal("atm")
                .executes(ctx -> {
                    ATMMenu.open(ctx.getSource().getPlayerOrException());
                    return 1;
                });
    }

    private static Item unwrapBlockToItem(Item item) {
        return switch (item.getDescriptionId()) {
            case "block.minecraft.netherite_block" -> Items.NETHERITE_INGOT;
            case "block.minecraft.diamond_block" -> Items.DIAMOND;
            case "block.minecraft.gold_block" -> Items.GOLD_INGOT;
            case "block.minecraft.emerald_block" -> Items.EMERALD;
            case "block.minecraft.lapis_block" -> Items.LAPIS_LAZULI;
            case "block.minecraft.iron_block" -> Items.IRON_INGOT;
            case "block.minecraft.copper_block" -> Items.COPPER_INGOT;
            case "block.minecraft.redstone_block" -> Items.REDSTONE;
            default -> item;
        };
    }

    private static void giveExactItems(ServerPlayer player, Item item, int totalCount) {
        int maxStack = item.getDefaultMaxStackSize();
        while (totalCount > 0) {
            int stackSize = Math.min(totalCount, maxStack);
            ItemStack stack = new ItemStack(item, stackSize);
            if (!player.getInventory().add(stack)) {
                ItemEntity itemEntity = player.drop(stack, false);
                if (itemEntity != null) itemEntity.setNoPickUpDelay();
            }
            totalCount -= stackSize;
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildMarket(CommandBuildContext buildContext) {
        return Commands.literal("market")
                .executes(EconomyCommands::listMarketPrices)
                .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .executes(ctx -> showItemPrice(ctx, ItemArgument.getItem(ctx, "item").item().value())));
    }

    private static int listMarketPrices(CommandContext<CommandSourceStack> ctx) {
        MarketState market = MarketState.getState();
        market.getAll().forEach((item, data) -> {
            double buyUnit = data.getBulkBuyCost(1);
            double sellUnit = data.getBulkSellPayout(1);
            double ratio = (data.getCurrentPrice() / data.getDefaultPrice() - 1) * 100.0;

            String trend = ratio >= 0 ? String.format(" (+%.1f%%)", ratio) : String.format(" (%.1f%%)", ratio);
            ChatFormatting trendColor = ratio >= 0 ? ChatFormatting.RED : ChatFormatting.GREEN; // High price = red buy, discount = green

            Component entry = Component.literal("• ").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(item.getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(String.format(" | Buy: $%.2f | Sell: $%.2f", buyUnit, sellUnit)).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(trend).withStyle(trendColor));

            ctx.getSource().sendSuccess(() -> entry, false);
        });

        return 1;
    }

    private static int showItemPrice(CommandContext<CommandSourceStack> ctx, Item item) {
        MarketState market = MarketState.getState();
        FluctuationData data = market.get(item);

        if (data == null) {
            ctx.getSource().sendFailure(Component.literal("This item is not tracked by the market."));
            return -1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(String.format(" Base Price: $%.2f", data.getDefaultPrice())).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(" 1x   Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(1), data.getBulkSellPayout(1))).withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(" 64x  Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(64), data.getBulkSellPayout(64))).withStyle(ChatFormatting.WHITE), false);

        return 1;
    }
}