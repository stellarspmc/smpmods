package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.utils.UtilClass;
import ml.spmc.smpmod.utils.sql.DatabaseManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class StatsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("stats").executes(StatsCommand::stats);
    }

    public static int stats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        String uuid = player.getStringUUID();
        DatabaseManager manager = UtilClass.getDatabaseManager();
        long playtime = manager.getPlaytime(uuid);
        int level = manager.getExpLvl(uuid)[1];
        int exp = manager.getExpLvl(uuid)[0];
        player.sendSystemMessage(Component.literal("Your Stats (Does not include IP and crates)").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Playtime: " + playtime));
        player.sendSystemMessage(Component.literal("Level:" + level));
        player.sendSystemMessage(Component.literal("XP:" + exp));
        player.sendSystemMessage(Component.literal("Last Online: NOW"));
        return Command.SINGLE_SUCCESS;
    }
}
