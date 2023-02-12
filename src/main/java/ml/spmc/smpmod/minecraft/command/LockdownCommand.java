package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.utils.UtilClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Objects;

public class LockdownCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("lock").executes(LockdownCommand::lock);
    }
    public static int lock(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayerOrThrow();
        if (!Objects.equals(player.getName().getString(), "tcfplayz") || !Objects.equals(player.getName().getString(), "eyelol")) return 0;
        if (!UtilClass.lockdown) {
            UtilClass.lockdown = true;
            SMPMod.SERVER.getPlayerManager().broadcast(Text.literal("balance locked"), false);
            SMPMod.MESSAGECHANNEL.sendMessage("balance locked").queue();
        } else {
            UtilClass.lockdown = false;
            SMPMod.SERVER.getPlayerManager().broadcast(Text.literal("balance unlocked"), false);
            SMPMod.MESSAGECHANNEL.sendMessage("balance unlocked").queue();
        }
        System.out.println("used lock cmd");
        return 1;
    }
}
