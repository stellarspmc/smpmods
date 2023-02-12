package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.utils.UtilClass;
import ml.spmc.smpmod.utils.sql.DatabaseManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StatsCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("stats").executes(StatsCommand::stats);
    }

    public static int stats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayerOrThrow();
        String uuid = player.getUuidAsString();
        DatabaseManager manager = UtilClass.getDatabaseManager();
        long playtime = manager.getPlaytime(uuid);
        int level = manager.getExpLvl(uuid)[1];
        int exp = manager.getExpLvl(uuid)[0];
        player.sendMessage(Text.literal("Your Stats (Does not include IP and crates)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal("Playtime: " + playtime));
        player.sendMessage(Text.literal("Level:" + level));
        player.sendMessage(Text.literal("XP:" + exp));
        player.sendMessage(Text.literal("Last Online: NOW"));
        return Command.SINGLE_SUCCESS;
    }
}
