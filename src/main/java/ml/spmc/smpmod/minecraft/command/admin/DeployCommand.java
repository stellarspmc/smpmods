package ml.spmc.smpmod.minecraft.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.SMPMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DeployCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("deploy").then(Commands.argument("smpversion", StringArgumentType.string()).executes(e -> deploy(e, StringArgumentType.getString(e, "smpversion"))));
    }

    public static int deploy(CommandContext<CommandSourceStack> ctx, String version) throws CommandSyntaxException {
        if (ctx.getSource().isPlayer()) {
            if (ctx.getSource().getPlayerOrException().getName().getString().equals("tcfplayz")) return deployConfirm(version);
            else return 0;
        } else return deployConfirm(version);
    }

    private static int deployConfirm(String version) {
        SMPMod.SERVER.getPlayerList().broadcastSystemMessage(Component.literal("SMP Update " + version + " coming out in 1-5 minutes!"), false);
        SMPMod.MESSAGECHANNEL.sendMessage("SMP Update " + version + " coming out in 1-5 minutes!").queue();
        return 1;
    }
}
