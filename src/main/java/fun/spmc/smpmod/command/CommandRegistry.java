package fun.spmc.smpmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.fishing.FishTracker;
import fun.spmc.smpmod.npc.NPCData;
import fun.spmc.smpmod.npc.NPCManager;
import fun.spmc.smpmod.quest.QuestManager;
import fun.spmc.smpmod.quest.PlayerQuestData;
import fun.spmc.smpmod.quest.Quest;
import fun.spmc.smpmod.utils.MessageUtils;
import fun.spmc.smpmod.vault.VaultData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static fun.spmc.smpmod.SMPMod.minecraftServer;
import static fun.spmc.smpmod.command.EconomyCommands.processItemDeposit;

@SuppressWarnings("UnstableApiUsage")
public class CommandRegistry {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection ignoredCommandSelection) {
        dispatcher.register(buildBalanceNode("bal"));
        dispatcher.register(buildBalanceNode("balance"));
        dispatcher.register(EconomyCommands.buildMarket(context));
        dispatcher.register(EconomyCommands.buildTop());
        dispatcher.register(EconomyCommands.buildWithdraw(context));
        dispatcher.register(Commands.literal("send")
                .then(Commands.argument("player", GameProfileArgument.gameProfile()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(.1)).executes((ctx -> {
                    NameAndId target = GameProfileArgument.getGameProfiles(ctx, "player").iterator().next();
                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                    double amount = Math.round(DoubleArgumentType.getDouble(ctx, "amount") * 100f) / 100f;

                    if (sender.getUUID().equals(target.id())) {
                        MessageUtils.sendErrorMessage(sender, "You cannot send money to yourself.");
                        return -1;
                    }

                    EconomyData eco = EconomyData.get();
                    if (eco.changeBalance(sender.getUUID(), -amount)) {
                        eco.changeBalance(target.id(), amount);

                        if (minecraftServer.getPlayerList().getPlayer(target.id()) != null)
                            Objects.requireNonNull(minecraftServer.getPlayerList().getPlayer(target.id())).sendSystemMessage(Component.literal("💰: ").withStyle(ChatFormatting.GREEN).append(Component.literal("You received ").withStyle(ChatFormatting.GOLD)).append(Component.literal(String.format("$%.2f", amount)).withStyle(ChatFormatting.RED)).append(Component.literal(" from ").withStyle(ChatFormatting.GOLD)).append(Component.literal(sender.getName().getString()).withStyle(ChatFormatting.RED)));

                        MessageUtils.sendSuccessMessage(sender, String.format("Sent $%.2f to %s.", amount, target.name()));
                        return 1;
                    }

                    MessageUtils.sendErrorMessage(sender, "Insufficient funds.");
                    return -1;
                }
        )))));

        dispatcher.register(Commands.literal("deposit")
                .executes(ctx -> {
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
                })
                .then(Commands.literal("all").executes(ctx -> {
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
                })));

        //dispatcher.register(Commands.literal("atm").executes(ctx -> {
        //                    ATMMenu.open(ctx.getSource().getPlayerOrException());
        //                    return 1;
        //                }));

        dispatcher.register(Commands.literal("npc")
                .then(Commands.literal("kill").then(Commands.argument("id", StringArgumentType.greedyString()).requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)).suggests((_, builder) -> SharedSuggestionProvider.suggest(NPCManager.getAllIds(), builder)).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    String id = StringArgumentType.getString(ctx, "id");

                    if (NPCData.get().hasNpc(id)) {
                        Entity entity = NPCData.get().getMannequin(level, id);
                        if (entity != null) {
                            NPCData.get().removeNpc(id);
                            entity.discard();
                            MessageUtils.sendSuccessMessage(ctx.getSource().getPlayerOrException(), "Mannequin killed!");
                            return 1;
                        }
                    }
                    MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Mannequin isn't alive!");
                    return 0;
                }))).then(Commands.literal("setup").then(Commands.argument("id", StringArgumentType.greedyString()).requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)).suggests((_, builder) -> SharedSuggestionProvider.suggest(NPCManager.getAllIds(), builder)).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    Vec3 pos = ctx.getSource().getPosition();
                    String id = StringArgumentType.getString(ctx, "id");

                    if (NPCData.get().hasNpc(id)) {
                        MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Mannequin already exists!");
                        return 0;
                    } if (NPCManager.spawn(id, level, BlockPos.containing(pos)) == null) {
                        MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Mannequin already exists / id doesn't exist!");
                        return 0;
                    }
                    MessageUtils.sendSuccessMessage(ctx.getSource().getPlayerOrException(), "Mannequin created successfully!");
                    return 1;
                })))
        );

        dispatcher.register(Commands.literal("fishing").executes(ctx -> FishTracker.openFishIndexMenu(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("vault").executes(ctx -> VaultData.sendVaultMessage(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("quests").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            QuestManager.get().checkAndResetRotations(player);
            List<PlayerQuestData.ActiveQuest> activeQuests = QuestManager.getQuests(player).getActiveQuests();

            player.sendSystemMessage(Component.literal("=== Active Quests ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

            if (activeQuests.isEmpty()) {
                player.sendSystemMessage(Component.literal(" You have no active quests.").withStyle(ChatFormatting.GRAY));
                return 1;
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
        }));

        dispatcher.register(Commands.literal("mapart").then(Commands.argument("url", StringArgumentType.greedyString()).executes(ctx -> {
            var source = ctx.getSource();
            var url = StringArgumentType.getString(ctx, "url");
            if (!(source.getEntity() instanceof ServerPlayer player)) return 0;

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                MessageUtils.sendErrorMessage(player, "Invalid URL! Must start with http:// or https://");
                return 0;
            }

            CompletableFuture.runAsync(() -> {
                try {
                    URL imageUrl = new URI(url).toURL();
                    BufferedImage img = ImageIO.read(imageUrl);

                    if (img == null) {
                        MessageUtils.sendErrorMessage(player, "Could not load image from the provided URL.");
                        return;
                    }

                    int mapW = Math.max(1, img.getWidth() / 128);
                    int mapH = Math.max(1, img.getHeight() / 128);
                    double cost = 300 * mapW * mapH;

                    minecraftServer.execute(() -> {
                        EconomyData eco = EconomyData.get();

                        if (eco.getBalance(player.getUUID()) < cost) {
                            MessageUtils.sendErrorMessage(player, String.format("Insufficient funds! You need $%.2f for a %dx%d map.", cost, mapW, mapH));
                            return;
                        }

                        if (eco.changeBalance(player.getUUID(), -cost)) {
                            minecraftServer.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS), String.format("image2map create %s %s", "none", url));
                            MessageUtils.sendSuccessMessage(player, String.format("Created a %dx%d map art for $%.2f!", mapW, mapH, cost));
                        }
                    });

                } catch (Exception e) {
                    minecraftServer.execute(() -> MessageUtils.sendErrorMessage(player, "Failed to process image URL: " + e.getMessage()));
                }
            });
            return 1;
        })));

        dispatcher.register(Commands.literal("surface").executes(ctx -> {
            Player player = ctx.getSource().getPlayerOrException();
            ServerLevel world = ctx.getSource().getLevel();
            int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(player.getX()), (int) Math.floor(player.getZ()));

            player.teleportTo(player.getX(), y, player.getZ());
            player.playSound(SoundEvents.WITHER_SHOOT, 3, .5f);
            return 1;
        }));

        dispatcher.register(Commands.literal("enderchest").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.openMenu(new SimpleMenuProvider((syncId, inventory, p) -> ChestMenu.threeRows(syncId, inventory, p.getEnderChestInventory()), Component.translatable("block.minecraft.ender_chest")));
            player.awardStat(Stats.OPEN_ENDERCHEST, 1);
            return 1;
        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildBalanceNode(String name) {
        return Commands.literal(name)
                .executes(ctx -> balanceCommand(ctx, new NameAndId(ctx.getSource().getPlayerOrException().getGameProfile())))
                .then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(ctx -> balanceCommand(ctx, GameProfileArgument.getGameProfiles(ctx, "player").iterator().next())));
    }

    private static int balanceCommand(CommandContext<CommandSourceStack> ctx, NameAndId target) {
        EconomyData eco = EconomyData.get();
        ctx.getSource().sendSuccess(() -> Component.literal("💰: ").withStyle(ChatFormatting.GREEN).append(Component.literal(target.name() + " has ").withStyle(ChatFormatting.GOLD)).append(Component.literal(String.format("$%.2f", eco.getBalance(target.id()))).withStyle(ChatFormatting.RED)).append(Component.literal(".").withStyle(ChatFormatting.GOLD)), false);
        return 1;
    }
}