package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.minecraft.economy.EconomyConfig;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
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

import java.util.Map;
import java.util.Set;

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

    private static double getDepositValue(Item item) {
        // 1. Check if the item is a compressed block variant
        Item baseItem = switch (item.getDescriptionId()) {
            case "block.minecraft.netherite_block" -> Items.NETHERITE_INGOT;
            case "block.minecraft.diamond_block"   -> Items.DIAMOND;
            case "block.minecraft.gold_block"      -> Items.GOLD_INGOT;
            case "block.minecraft.emerald_block"   -> Items.EMERALD;
            case "block.minecraft.lapis_block"     -> Items.LAPIS_LAZULI;
            case "block.minecraft.iron_block"      -> Items.IRON_INGOT;
            case "block.minecraft.copper_block"    -> Items.COPPER_INGOT;
            default -> null; // Not a currency block
        };

        if (baseItem != null) return (EconomyConfig.getItemValue(baseItem) * 9) * 0.93;
        return EconomyConfig.getItemValue(item);
    }

    private static int depositHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack hand = player.getInventory().getSelectedItem();
        double itemValue = getDepositValue(hand.getItem());

        if (hand.isEmpty() || itemValue <= 0) {
            ctx.getSource().sendFailure(Component.literal("✖: Hold a valid currency item or use /deposit all.")
                    .withStyle(ChatFormatting.RED));
            return -1;
        }

        double totalValue = hand.getCount() * itemValue;
        EconomySavedData eco = EconomySavedData.get(player.level());

        if (eco.changeBalance(player.getUUID(), totalValue)) {
            player.getInventory().removeFromSelected(true);

            ctx.getSource().sendSuccess(() -> Component.literal("\uD83D\uDCB0: ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Deposited ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("$%.2f", totalValue)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" to your account.").withStyle(ChatFormatting.GRAY)), false);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("✖: Max balance reached.")
                .withStyle(ChatFormatting.RED));
        return -1;
    }

    private static int depositAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EconomySavedData eco = EconomySavedData.get(player.level());
        double totalDeposited = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            double itemValue = getDepositValue(stack.getItem());

            if (itemValue > 0 && !stack.isEmpty()) {
                double stackValue = stack.getCount() * itemValue;
                totalDeposited += stackValue;
                player.getInventory().removeItem(i, stack.getCount());
            }
        }

        if (totalDeposited > 0) {
            eco.changeBalance(player.getUUID(), totalDeposited);
            double finalTotal = totalDeposited;

            ctx.getSource().sendSuccess(() -> Component.literal("\uD83D\uDCB0: ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Deposited ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("$%.2f", finalTotal)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" to your account.").withStyle(ChatFormatting.GRAY)), false);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("✖: No valid currency items found in inventory.")
                .withStyle(ChatFormatting.RED));
        return -1;
    }

    private static final Set<Item> WITHDRAWABLE_CURRENCIES = Set.of(
            Items.NETHER_STAR,
            Items.NETHERITE_INGOT,
            Items.DIAMOND,
            Items.GOLD_INGOT,
            Items.EMERALD,
            Items.LAPIS_LAZULI,
            Items.IRON_INGOT,
            Items.COPPER_INGOT
    );

    public static LiteralArgumentBuilder<CommandSourceStack> buildWithdraw(CommandBuildContext buildContext) {
        return Commands.literal("withdraw")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.10))
                        .executes(ctx -> {
                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                            return withdrawCommand(ctx, amount);
                        }))
                .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .suggests((_, builder) -> SharedSuggestionProvider.suggestResource(
                                WITHDRAWABLE_CURRENCIES.stream().map(BuiltInRegistries.ITEM::getKey),
                                builder
                        ))
                        .executes(ctx -> {
                            Item item = ItemArgument.getItem(ctx, "item").item().value();

                            if (!WITHDRAWABLE_CURRENCIES.contains(item)) {
                                ctx.getSource().sendFailure(Component.literal("✖: You can only withdraw the designated items.").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            return withdrawItemCommand(ctx, item, 1);
                        })
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 6400))
                                .executes(ctx -> {
                                    Item item = ItemArgument.getItem(ctx, "item").item().value();
                                    int count = IntegerArgumentType.getInteger(ctx, "count");

                                    if (!WITHDRAWABLE_CURRENCIES.contains(item)) {
                                        ctx.getSource().sendFailure(Component.literal("✖: You can only withdraw the designated items.").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    return withdrawItemCommand(ctx, item, count);
                                })));
    }

    private static int withdrawItemCommand(CommandContext<CommandSourceStack> ctx, Item item, int count) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EconomySavedData eco = EconomySavedData.get(player.level());
        Double val = EconomyConfig.getSortedCurrencyValues().get(item);

        if (val == null || val <= 0) {
            ctx.getSource().sendFailure(Component.literal("✖: ")
                    .withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal(item.getDescriptionId() + " is not a valid currency item.")
                            .withStyle(ChatFormatting.RED)));
            return -1;
        }

        double totalCost = Math.round((val * count) * 100.0) / 100.0;

        if (eco.changeBalance(player.getUUID(), -totalCost)) {
            giveExactItems(player, item, count);

            ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("You withdrew ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(count + "x " + item.getDescriptionId()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" for ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.format("$%.2f", totalCost)).withStyle(ChatFormatting.RED))
                    .append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("✖: Insufficient balance. You need ").withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal(String.format("$%.2f", totalCost)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" to withdraw " + count + "x " + item.getDescriptionId() + ".").withStyle(ChatFormatting.DARK_RED)));
            return -1;
        }
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

    private static int withdrawCommand(CommandContext<CommandSourceStack> ctx, double amount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EconomySavedData eco = EconomySavedData.get(player.level());

        amount = Math.round(amount * 100.0) / 100.0;

        if (eco.changeBalance(player.getUUID(), -amount)) {
            double remainingUnconverted = dropItem(amount, player);

            if (remainingUnconverted > 0) eco.changeBalance(player.getUUID(), remainingUnconverted);

            double withdrawnAmount = amount - remainingUnconverted;
            ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("You withdrew ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.format("$%.2f", withdrawnAmount)).withStyle(ChatFormatting.RED))
                    .append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("✖: Insufficient balance to withdraw ").withStyle(ChatFormatting.DARK_RED)
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
                                        target.sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                                                .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.RED))
                                                .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(sender.getName().getString()).withStyle(ChatFormatting.RED)));

                                        ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                                                .append(Component.literal("You sent ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(String.format("$%.2f", finalAmount)).withStyle(ChatFormatting.RED))
                                                .append(Component.literal(" to ").withStyle(ChatFormatting.GOLD))
                                                .append(Component.literal(target.getName().getString()).withStyle(ChatFormatting.RED)), false);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("✖: Insufficient funds.").withStyle(ChatFormatting.DARK_RED));
                                        return -1;
                                    }
                                })));
    }
}