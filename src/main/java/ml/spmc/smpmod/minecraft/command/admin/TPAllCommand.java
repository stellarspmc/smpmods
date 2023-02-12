package ml.spmc.smpmod.minecraft.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.SMPMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.Objects;

public class TPAllCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("tpall").executes(TPAllCommand::tpall);
    }

    public static int tpall(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayerOrThrow();
        if (!Objects.equals(player.getName().getString(), "tcfplayz")) return 0;
        else SMPMod.SERVER.getPlayerManager().getPlayerList().forEach(player1 -> player1.teleport((ServerWorld) player.getWorld(), player.getX(), player.getY(), player.getZ(), 1f, 1f));
        return 1;
    }
}
