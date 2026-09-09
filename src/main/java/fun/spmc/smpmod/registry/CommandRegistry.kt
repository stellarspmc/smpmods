package fun.spmc.smpmod.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.economy.fluctuate.FluctuationData;
import fun.spmc.smpmod.economy.fluctuate.MarketState;
import fun.spmc.smpmod.fishing.FishTracker;
import fun.spmc.smpmod.npc.NPCData;
import fun.spmc.smpmod.npc.NPCManager;
import fun.spmc.smpmod.quest.QuestManager;
import fun.spmc.smpmod.quest.PlayerQuestData;
import fun.spmc.smpmod.quest.Quest;
import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.utils.UtilityFunctions;
import fun.spmc.smpmod.vault.VaultData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

@SuppressWarnings("UnstableApiUsage")
public class CommandRegistry {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection ignoredCommandSelection) {
        dispatcher.register(buildBalanceNode("bal"));
        dispatcher.register(buildBalanceNode("balance"));
        dispatcher.register(Commands.literal("baltop").executes(ctx -> executeTop(ctx, 1)).then(Commands.argument("page", IntegerArgumentType.integer(1)).executes(ctx -> executeTop(ctx, IntegerArgumentType.getInteger(ctx, "page")))));
        dispatcher.register(Commands.literal("send").then(Commands.argument("player", GameProfileArgument.gameProfile()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1)).executes(CommandRegistry::executeSend))));
        dispatcher.register(Commands.literal("deposit").executes(CommandRegistry::executeDepositHand).then(Commands.literal("all").executes(CommandRegistry::executeDepositAll)));
        dispatcher.register(Commands.literal("market").executes(CommandRegistry::executeMarketAll).then(Commands.argument("item", ItemArgument.item(context)).suggests(UtilityFunctions.streamToSuggestion(MarketState.getState().getAll().keySet().stream()))).executes(CommandRegistry::executeMarketItem));
        dispatcher.register(Commands.literal("withdraw").then(Commands.argument("item", ItemArgument.item(context)).suggests(UtilityFunctions.streamToSuggestion(Stream.concat(MarketState.getState().getAll().keySet().stream(), Stream.of(Items.DIAMOND)))).executes(ctx -> executeWithdraw(ctx, 1)).then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(ctx -> executeWithdraw(ctx, IntegerArgumentType.getInteger(ctx, "count"))))));

        dispatcher.register(Commands.literal("fishing").executes(ctx -> FishTracker.openFishIndexMenu(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("vault").executes(ctx -> VaultData.sendVaultMessage(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("quests").executes(CommandRegistry::executeQuests));
        dispatcher.register(Commands.literal("mapart").then(Commands.argument("url", StringArgumentType.greedyString()).executes(CommandRegistry::executeMapArt)));
        dispatcher.register(Commands.literal("surface").executes(CommandRegistry::executeSurface));
        dispatcher.register(Commands.literal("enderchest").executes(CommandRegistry::executeEnderChest));
        dispatcher.register(buildNpcNode());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildBalanceNode(String name) {
        return Commands.literal(name).executes(ctx -> executeBalance(ctx, new NameAndId(ctx.getSource().getPlayerOrException().getGameProfile()))).then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(ctx -> executeBalance(ctx, GameProfileArgument.getGameProfiles(ctx, "player").iterator().next())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildNpcNode() {
        return Commands.literal("npc")
                .then(Commands.literal("kill")
                        .then(Commands.argument("id", StringArgumentType.greedyString())
                                .requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS))
                                .suggests((_, builder) -> SharedSuggestionProvider.suggest(NPCManager.getAllIds(), builder))
                                .executes(CommandRegistry::executeNpcKill)))
                .then(Commands.literal("setup")
                        .then(Commands.argument("id", StringArgumentType.greedyString())
                                .requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS))
                                .suggests((_, builder) -> SharedSuggestionProvider.suggest(NPCManager.getAllIds(), builder))
                                .executes(CommandRegistry::executeNpcSetup)));
    }

    private static int executeBalance(CommandContext<CommandSourceStack> ctx, NameAndId target) {
        EconomyData eco = EconomyData.get();
        ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(target.name() + " has ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("$%.2f", eco.getBalance(target.id()))).withStyle(ChatFormatting.RED))
                .append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
        return 1;
    }

    private static int executeTop(CommandContext<CommandSourceStack> ctx, int page) {
        EconomyData eco = EconomyData.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Wealth Leaderboard").withStyle(ChatFormatting.GOLD).append("\n").append(eco.getMinecraftTop(page)), false);
        return 1;
    }

    private static int executeSend(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        NameAndId target = GameProfileArgument.getGameProfiles(ctx, "player").iterator().next();
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        double amount = Math.round(DoubleArgumentType.getDouble(ctx, "amount") * 100f) / 100f;

        if (sender.getUUID().equals(target.id())) return MessageUtils.sendError(sender, "You cannot send money to yourself.", 0);

        EconomyData eco = EconomyData.get();
        if (eco.changeBalance(sender.getUUID(), -amount)) {
            eco.changeBalance(target.id(), amount);

            ServerPlayer targetPlayer = minecraftServer.getPlayerList().getPlayer(target.id());
            if (targetPlayer != null) {
                targetPlayer.sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("You received ").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.RED))
                        .append(Component.literal(" from ").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(sender.getName().getString()).withStyle(ChatFormatting.RED)));
            }

            return MessageUtils.sendSuccess(sender, String.format("Sent $%.2f to %s.", amount, target.name()), 1);
        }

        return MessageUtils.sendError(sender, "Insufficient funds.", 0);
    }

    private static int executeDepositHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack hand = player.getInventory().getSelectedItem();

        if (hand.isEmpty()) return MessageUtils.sendError(player, "Hold a valid market item or use /deposit all.", 0);
        double payout = MarketState.processItemDeposit(player, hand);
        if (payout <= 0) return MessageUtils.sendError(player, "This item cannot be deposited into the market.", 0);
        hand.setCount(0);
        return MessageUtils.sendSuccess(player, String.format("Deposited items for $%.2f to your account.", payout), 1);
    }

    private static int executeDepositAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        double totalPayout = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            double payout = MarketState.processItemDeposit(player, stack);
            if (payout > 0) {
                totalPayout += payout;
                player.getInventory().removeItem(i, stack.getCount());
            }
        }

        if (totalPayout > 0) return MessageUtils.sendSuccess(player, String.format("Deposited all valid items for $%.2f to your account.", totalPayout), 1);
        return MessageUtils.sendError(player, "No valid market currency items found in inventory.", 0);
    }

    private static int executeNpcKill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        String id = StringArgumentType.getString(ctx, "id");

        if (NPCData.get().hasNpc(id)) {
            Entity entity = NPCData.get().getMannequin(level, id);
            if (entity != null) {
                NPCData.get().removeNpc(id);
                entity.discard();
                return MessageUtils.sendSuccess(ctx.getSource().getPlayerOrException(), "Mannequin killed!", 1);
            }
        }
        return MessageUtils.sendError(ctx.getSource().getPlayerOrException(), "Mannequin isn't alive!", 0);
    }

    private static int executeNpcSetup(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        Vec3 pos = ctx.getSource().getPosition();
        String id = StringArgumentType.getString(ctx, "id");

        if (NPCData.get().hasNpc(id)) return MessageUtils.sendError(ctx.getSource().getPlayerOrException(), "Mannequin already exists!", 0);
        if (NPCManager.spawn(id, level, BlockPos.containing(pos)) == null) return MessageUtils.sendError(ctx.getSource().getPlayerOrException(), "Mannequin already exists / id doesn't exist!", 0);
        return MessageUtils.sendSuccess(ctx.getSource().getPlayerOrException(), "Mannequin created successfully!", 1);
    }

    private static int executeQuests(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestManager.get().checkAndResetRotations(player);
        List<PlayerQuestData.ActiveQuest> activeQuests = QuestManager.getQuests(player).getActiveQuests();
        player.sendSystemMessage(Component.literal("=== Active Quests ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        if (activeQuests.isEmpty()) {
            player.sendSystemMessage(Component.literal(" You have no active quests.").withStyle(ChatFormatting.GRAY));
            return 0;
        }

        for (PlayerQuestData.ActiveQuest activeQuest : activeQuests) {
            Quest quest = activeQuest.getQuest();
            if (quest == null) continue;

            ChatFormatting categoryColor = switch (quest.questType()) {
                case DAILY -> ChatFormatting.YELLOW;
                case WEEKLY -> ChatFormatting.LIGHT_PURPLE;
                case NPC -> ChatFormatting.AQUA;
            };

            MutableComponent questLine = Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("[" + quest.questType().getSerializedName().toUpperCase() + "] ").withStyle(categoryColor, ChatFormatting.BOLD))
                    .append(Component.literal(quest.title()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY));

            if (activeQuest.isCompleted()) {
                if (activeQuest.isClaimed()) questLine.append(Component.literal("Completed").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
                else questLine.append(Component.literal("READY TO CLAIM!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            } else questLine.append(Component.literal(activeQuest.getCurrentCount() + "/" + quest.requiredCount()).withStyle(ChatFormatting.AQUA));

            player.sendSystemMessage(questLine);
            if (!quest.description().isEmpty()) player.sendSystemMessage(Component.literal("   " + quest.description()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        return 1;
    }

    private static int executeMapArt(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var url = StringArgumentType.getString(ctx, "url");

        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return MessageUtils.sendError(player, "Invalid URL! Must start with http:// or https://", 0);

        CompletableFuture.runAsync(() -> {
            try {
                URL imageUrl = new URI(url).toURL();
                BufferedImage img = ImageIO.read(imageUrl);

                if (img == null) {
                    MessageUtils.sendError(player, "Could not load image from the provided URL.", 0);
                    return;
                }

                int mapW = Math.max(1, img.getWidth() / 128);
                int mapH = Math.max(1, img.getHeight() / 128);
                double cost = 300 * mapW * mapH;

                minecraftServer.execute(() -> {
                    EconomyData eco = EconomyData.get();

                    if (eco.getBalance(player.getUUID()) < cost) {
                        MessageUtils.sendError(player, String.format("Insufficient funds! You need $%.2f for a %dx%d map.", cost, mapW, mapH), 0);
                        return;
                    }

                    if (eco.changeBalance(player.getUUID(), -cost)) {
                        minecraftServer.getCommands().performPrefixedCommand(
                                player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS),
                                String.format("image2map create %s %s", "none", url)
                        );
                        MessageUtils.sendSuccess(player, String.format("Created a %dx%d map art for $%.2f!", mapW, mapH, cost), 1);
                    }
                });

            } catch (Exception e) {
                minecraftServer.execute(() -> MessageUtils.sendError(player, "Failed to process image URL: " + e.getMessage(), 0));
            }
        });
        return 1;
    }

    private static int executeSurface(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        player.teleportTo(player.getX(), ctx.getSource().getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(player.getX()), (int) Math.floor(player.getZ())), player.getZ());
        player.playSound(SoundEvents.WITHER_SHOOT, 3, .5f);
        return 1;
    }

    private static int executeEnderChest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.openMenu(new SimpleMenuProvider((syncId, inventory, p) -> ChestMenu.threeRows(syncId, inventory, p.getEnderChestInventory()), Component.translatable("block.minecraft.ender_chest")));
        player.awardStat(Stats.OPEN_ENDERCHEST, 1);
        return 1;
    }

    private static int executeMarketAll(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("Market Prices").withStyle(ChatFormatting.GOLD), false);
        MarketState.getState().getAll().values().stream()
                .sorted((e1, e2) -> Double.compare(e2.getDefaultPrice(), e1.getDefaultPrice()))
                .forEach((data) -> {
                    double buyUnit = data.getBulkBuyCost(1);
                    double sellUnit = data.getBulkSellPayout(1);
                    double ratio = (data.getCurrentPrice() / data.getDefaultPrice() - 1) * 100.0;

                    String trend = ratio > 0 ? String.format(" (+%.1f%%)", ratio) : String.format(" (%.1f%%)", ratio);
                    ChatFormatting trendColor = ratio >= 0 ? ((ratio == 0) ? ChatFormatting.GRAY : ChatFormatting.RED) : ChatFormatting.GREEN;

                    Component message = Component.literal("• ").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable(data.getMineral().getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(String.format(" | Buy: $%.2f | Sell: $%.2f", buyUnit, sellUnit)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(trend).withStyle(trendColor));

                    ctx.getSource().sendSuccess(() -> message, false);
                });

        return 1;
    }

    private static int executeMarketItem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Item targetItem = ItemArgument.getItem(ctx, "item").item().value();
        MarketState market = MarketState.getState();

        FluctuationData data = market.get(targetItem);
        if (data == null) return MessageUtils.sendError(ctx.getSource().getPlayerOrException(), "This item is not tracked by the market.", 0);

        ctx.getSource().sendSuccess(() -> Component.literal(String.format(" Base Price: $%.2f", data.getDefaultPrice())).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(" 1x   Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(1), data.getBulkSellPayout(1))).withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(" 64x  Buy: $%.2f  |  Sell: $%.2f", data.getBulkBuyCost(64), data.getBulkSellPayout(64))).withStyle(ChatFormatting.WHITE), false);
        return 1;
    }

    private static int executeWithdraw(CommandContext<CommandSourceStack> ctx, int count) throws CommandSyntaxException {
        Item item = ItemArgument.getItem(ctx, "item").item().value();
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        double totalCost = MarketState.buyMineral(player, item, count);
        if (totalCost == -2) {
            player.sendSystemMessage(Component.literal("✖: ").append(Component.translatable(item.getDescriptionId())).append(" is not a tradeable market item.").withStyle(ChatFormatting.RED));
            return -1;
        } else if (totalCost == -1) {
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
}