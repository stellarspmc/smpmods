package fun.spmc.smpmod.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class MapArtCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("mapart")
                .then(Commands.argument("dither", StringArgumentType.word())
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> processMapArt(ctx.getSource(), StringArgumentType.getString(ctx, "dither"), StringArgumentType.getString(ctx, "url")))));
    }

    private static int processMapArt(CommandSourceStack source, String dither, String url) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        if (!dither.equalsIgnoreCase("dither") && !dither.equalsIgnoreCase("none")) {
            MessageUtils.sendErrorMessage(player, "Invalid dither option! Use 'dither' or 'none'.");
            return 0;
        }

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
                        minecraftServer.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS), String.format("image2map create %s %s", dither.toLowerCase(), url));
                        MessageUtils.sendSuccessMessage(player, String.format("Created a %dx%d map art for $%.2f!", mapW, mapH, cost));
                    }
                });

            } catch (Exception e) {
                minecraftServer.execute(() -> MessageUtils.sendErrorMessage(player, "Failed to process image URL: " + e.getMessage()));
            }
        });

        return 1;
    }
}