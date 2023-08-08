package ml.spmc.smpmod.minecraft.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.SMPMod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class DeployCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("deploy").then(CommandManager.argument("smpversion", StringArgumentType.string()).executes(e -> deploy(e, StringArgumentType.getString(e, "smpversion"))));
    }

    public static int deploy(CommandContext<ServerCommandSource> ctx, String version) throws CommandSyntaxException {
        if (ctx.getSource().isExecutedByPlayer()) {
            if (ctx.getSource().getPlayerOrThrow().getName().getString().equals("tcfplayz")) return deployConfirm(version);
            else return 0;
        } else return deployConfirm(version);
    }

    private static int deployConfirm(String version) {
        SMPMod.minecraftServer.getPlayerManager().getPlayerList().forEach((player) -> player.sendMessage(Text.literal("SMP Update " + version + " will be rolling out."), false));
        SMPMod.messageChannel.sendMessage("SMP Update " + version + " will be rolling out.").queue();
        return 1;
    }
}
