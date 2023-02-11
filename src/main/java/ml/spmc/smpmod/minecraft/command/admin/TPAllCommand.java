package ml.spmc.smpmod.minecraft.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.SMPMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TPAllCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("tpall").executes(TPAllCommand::tpall);
    }

    public static int tpall(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (player.getName().getString() != "tcfplayz") return 0;
        else SMPMod.SERVER.getPlayerList().getPlayers().forEach(player1 -> player1.teleportTo(player.getLevel(), player.getX(), player.getY(), player.getZ(), 1f, 1f));
        return 1;
    }
}
