package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class LockdownCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("lock").executes(LockdownCommand::lock);
    }
    public static int lock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        if (!Objects.equals(player.getName().getString(), "tcfplayz") || !Objects.equals(player.getName().getString(), "eyelol")) return 0;
        if (!UtilClass.lockdown) {
            UtilClass.lockdown = true;
            SMPMod.SERVER.getPlayerList().broadcastSystemMessage(Component.literal("balance locked"), false);
            SMPMod.MESSAGECHANNEL.sendMessage("balance locked").queue();
        } else {
            UtilClass.lockdown = false;
            SMPMod.SERVER.getPlayerList().broadcastSystemMessage(Component.literal("balance unlocked"), false);
            SMPMod.MESSAGECHANNEL.sendMessage("balance unlocked").queue();
        }
        System.out.println("used lock cmd");
        return 1;
    }
}
