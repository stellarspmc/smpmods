package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class MapArtCommand {

    private static final int MAX_MAP_SIZE = 5;

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("mapart")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                        .executes(ctx -> processMapArt(ctx.getSource(), "none", StringArgumentType.getString(ctx, "url"), -1, -1)))

                .then(Commands.argument("dither", StringArgumentType.word())
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> processMapArt(ctx.getSource(), StringArgumentType.getString(ctx, "dither"), StringArgumentType.getString(ctx, "url"), -1, -1))))

                .then(Commands.argument("width", IntegerArgumentType.integer(1, MAX_MAP_SIZE))
                        .then(Commands.argument("height", IntegerArgumentType.integer(1, MAX_MAP_SIZE))
                                .then(Commands.argument("dither", StringArgumentType.word())
                                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                                .executes(ctx -> processMapArt(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "dither"),
                                                        StringArgumentType.getString(ctx, "url"),
                                                        IntegerArgumentType.getInteger(ctx, "width"),
                                                        IntegerArgumentType.getInteger(ctx, "height")
                                                ))))));
    }

    private static int processMapArt(CommandSourceStack source, String dither, String url, int manualWidth, int manualHeight) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can execute this command."));
            return 0;
        }

        if (!dither.equalsIgnoreCase("dither") && !dither.equalsIgnoreCase("none")) {
            player.sendSystemMessage(Component.literal("✖ Invalid dither option! Use 'dither' or 'none'.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            player.sendSystemMessage(Component.literal("✖ Invalid URL! Must start with http:// or https://").withStyle(ChatFormatting.RED));
            return 0;
        }

        player.sendSystemMessage(Component.literal("🎨 Inspecting image dimensions...").withStyle(ChatFormatting.GRAY));

        CompletableFuture.runAsync(() -> {
            try {
                int finalWidth;
                int finalHeight;

                if (manualWidth > 0 && manualHeight > 0) {
                    finalWidth = manualWidth;
                    finalHeight = manualHeight;
                } else {
                    URL imageUrl = new URI(url).toURL();
                    BufferedImage img = ImageIO.read(imageUrl);

                    if (img == null) {
                        player.sendSystemMessage(Component.literal("✖ Could not load image from the provided URL.").withStyle(ChatFormatting.RED));
                        return;
                    }

                    int imgW = img.getWidth();
                    int imgH = img.getHeight();

                    int mapW = Math.max(1, (int) Math.ceil(imgW / 128.0));
                    int mapH = Math.max(1, (int) Math.ceil(imgH / 128.0));

                    if (mapW > MAX_MAP_SIZE || mapH > MAX_MAP_SIZE) {
                        double scale = Math.min((double) MAX_MAP_SIZE / mapW, (double) MAX_MAP_SIZE / mapH);
                        mapW = Math.max(1, (int) Math.round(mapW * scale));
                        mapH = Math.max(1, (int) Math.round(mapH * scale));
                    }

                    finalWidth = mapW;
                    finalHeight = mapH;
                }

                double cost = 300.0 * finalWidth * finalHeight;

                minecraftServer.execute(() -> {
                    ServerLevel level = player.level();
                    EconomySavedData eco = EconomySavedData.get(level);

                    if (eco.getBalance(player.getUUID()) < cost) {
                        player.sendSystemMessage(Component.literal(String.format("✖ Insufficient funds! You need $%.2f for a %dx%d map.", cost, finalWidth, finalHeight)).withStyle(ChatFormatting.RED));
                        return;
                    }

                    if (eco.changeBalance(player.getUUID(), -cost)) {
                        CommandSourceStack elevatedSource = player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);
                        String internalCmd = String.format("image2map create %d %d %s %s", finalWidth, finalHeight, dither.toLowerCase(), url);

                        minecraftServer.getCommands().performPrefixedCommand(elevatedSource, internalCmd);
                        player.sendSystemMessage(Component.literal(String.format("🎨 Created a %dx%d map art (original aspect ratio) for $%.2f!", finalWidth, finalHeight, cost)).withStyle(ChatFormatting.GREEN));
                    }
                });

            } catch (Exception e) {
                minecraftServer.execute(() ->
                        player.sendSystemMessage(Component.literal("✖ Failed to process image URL: " + e.getMessage()).withStyle(ChatFormatting.RED))
                );
            }
        });

        return 1;
    }
}