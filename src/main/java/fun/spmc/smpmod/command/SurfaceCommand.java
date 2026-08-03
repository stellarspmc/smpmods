package fun.spmc.smpmod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("surface").executes(SurfaceCommand::surface);
    }

    public static int surface(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ServerLevel world = context.getSource().getLevel();
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(player.getX()), (int) Math.floor(player.getZ()));

        player.teleportTo(player.getX(), y, player.getZ());
        player.playSound(SoundEvents.WITHER_SHOOT, 1, 1);
        return 1;
    }
}
