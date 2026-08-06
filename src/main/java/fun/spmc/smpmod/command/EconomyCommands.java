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
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class EconomyCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> buildBalance() {
        return Commands.literal("bal")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> balanceCommand(ctx, null, GameProfileArgument.getGameProfiles(ctx, "player").iterator().next())))
                .executes(ctx -> balanceCommand(ctx, ctx.getSource().getPlayerOrException(), null));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildBalanceAlias() {
        return Commands.literal("balance")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> balanceCommand(ctx, null, GameProfileArgument.getGameProfiles(ctx, "player").iterator().next())))
                .executes(ctx -> balanceCommand(ctx, ctx.getSource().getPlayerOrException(), null));
    }

    private static int balanceCommand(CommandContext<CommandSourceStack> ctx, @Nullable ServerPlayer target, @Nullable NameAndId id) {
        EconomySavedData eco = EconomySavedData.get();
        String name = (target != null) ? "You" : Objects.requireNonNull(id).name();
        double bal = (target != null) ? eco.getBalance(target.getUUID()) : eco.getBalance(Objects.requireNonNull(id).id());
        ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(name + " have ").withStyle(ChatFormatting.GOLD))
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

    public static double processItemDeposit(ServerPlayer player, ItemStack stack) {
        Item baseItem = unwrapBlockToItem(stack.getItem());
        int totalUnits = stack.getCount() * ((baseItem != stack.getItem()) ? 9 : 1);
        double blockTax = (baseItem != stack.getItem()) ? .93 : 1;
        return MarketState.sellMineral(player, baseItem, totalUnits, blockTax);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildWithdraw(CommandBuildContext buildContext) {
        return Commands.literal("withdraw")
                .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .suggests((_, builder) -> SharedSuggestionProvider.suggestResource(
                                Stream.concat(MarketState.getState().getAll().keySet().stream(), Stream.of(Items.DIAMOND)).distinct().map(BuiltInRegistries.ITEM::getKey),
                                builder
                        ))
                        .executes(ctx -> handleWithdrawItem(ctx, 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 6400))
                                .executes(ctx -> handleWithdrawItem(ctx, IntegerArgumentType.getInteger(ctx, "count")))));
    }

    private static int handleWithdrawItem(CommandContext<CommandSourceStack> ctx, int count) throws CommandSyntaxException {
        Item item = ItemArgument.getItem(ctx, "item").item().value();
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (item == Items.DIAMOND) {
            EconomySavedData.get().changeBalance(player.getUUID(), -count * 100);
            giveExactItems(player, Items.DIAMOND, count);
            MessageUtils.sendSuccessMessage(player, String.format("Withdrew %dx Diamonds for $%d.", count, count * 100));
            return 1;
        }

        double totalCost = MarketState.buyMineral(player, item, count);

        if (totalCost == -2) {
            player.sendSystemMessage(Component.literal("✖: ").append(Component.translatable(item.getDescriptionId())).append(" is not a tradeable market item.").withStyle(ChatFormatting.RED));
            return -1;
        }
        if (totalCost == -1) {
            MarketState market = MarketState.getState();
            FluctuationData data = market.get(item);
            double estimatedCost = data != null ? data.getBulkBuyCost(count) : 0;
            player.sendSystemMessage(Component.literal(String.format("✖: Insufficient balance. You need $%.2f to withdraw %dx ", estimatedCost, count)).append(Component.translatable(item.getDescriptionId())).append(".").withStyle(ChatFormatting.RED));
            return -1;
        }

        giveExactItems(player, item, count);
        player.sendSystemMessage(Component.literal(String.format("🏢: Withdrew %dx ", count)).append(Component.translatable(item.getDescriptionId())).append(String.format(" for $%.2f.", totalCost)).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildSend() {
        return Commands.literal("send")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(.1))
                .executes((ctx -> {
                    NameAndId target = GameProfileArgument.getGameProfiles(ctx, "player").iterator().next();
                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                    double amount = Math.round(DoubleArgumentType.getDouble(ctx, "amount") * 100f) / 100f;

                    if (sender.getUUID().equals(target.id())) {
                        MessageUtils.sendErrorMessage(sender, "You cannot send money to yourself.");
                        return -1;
                    }

                    EconomySavedData eco = EconomySavedData.get();
                    if (eco.changeBalance(sender.getUUID(), -amount)) {
                        eco.changeBalance(target.id(), amount);

                        if (minecraftServer.getPlayerList().getPlayer(target.id()) != null)
                            Objects.requireNonNull(minecraftServer.getPlayerList().getPlayer(target.id())).sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                                .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.RED))
                                .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(sender.getName().getString()).withStyle(ChatFormatting.RED)));

                        MessageUtils.sendSuccessMessage(sender, String.format("Sent $%.2f to %s.", amount, target.name()));
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
                        .suggests((_, builder) -> SharedSuggestionProvider.suggestResource(
                                MarketState.getState().getAll().keySet().stream().map(BuiltInRegistries.ITEM::getKey),
                                builder
                        ))
                        .executes(ctx -> showItemPrice(ctx, ItemArgument.getItem(ctx, "item").item().value())));
    }

    private static int listMarketPrices(CommandContext<CommandSourceStack> ctx) {
        MarketState market = MarketState.getState();
        ctx.getSource().sendSuccess(() -> Component.literal("Market Prices").withStyle(ChatFormatting.GOLD), false);
        market.getAll().entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getDefaultPrice(), e1.getValue().getDefaultPrice()))
                .forEach((entry) -> {
                    Item item = entry.getKey();
                    FluctuationData data = entry.getValue();

                    double buyUnit = data.getBulkBuyCost(1);
                    double sellUnit = data.getBulkSellPayout(1);
                    double ratio = (data.getCurrentPrice() / data.getDefaultPrice() - 1) * 100.0;

                    String trend = ratio > 0 ? String.format(" (+%.1f%%)", ratio) : String.format(" (%.1f%%)", ratio);
                    ChatFormatting trendColor = ratio >= 0 ? ((ratio == 0) ? ChatFormatting.GRAY : ChatFormatting.RED) : ChatFormatting.GREEN;

                    Component message = Component.literal("• ").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable(item.getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(String.format(" | Buy: $%.2f | Sell: $%.2f", buyUnit, sellUnit)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(trend).withStyle(trendColor));

                    ctx.getSource().sendSuccess(() -> message, false);
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

    public static LiteralArgumentBuilder<CommandSourceStack> buildTop() {
        return Commands.literal("baltop")
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> topCommand(ctx, IntegerArgumentType.getInteger(ctx, "page"))))
                .executes(ctx -> topCommand(ctx, 1));
    }

    private static int topCommand(CommandContext<CommandSourceStack> ctx, int page) {
        EconomySavedData eco = EconomySavedData.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Wealth Leaderboard").withStyle(ChatFormatting.GOLD)
                .append("\n" + eco.getMinecraftTop(page)), false);
        return 1;
    }
}