package fun.spmc.smpmod.minecraft.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MessageUtils {

    public static void sendErrorMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("✖: " + message).withStyle(ChatFormatting.RED));
    }

    public static void sendSuccessMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("🏢: " + message).withStyle(ChatFormatting.GREEN));
    }
}
