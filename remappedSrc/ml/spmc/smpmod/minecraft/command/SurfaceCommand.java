package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.Command;
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
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("surface").executes(SurfaceCommand::surface);
    }

    public static int surface(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ServerLevel world = context.getSource().getLevel();

        assert player != null;

        int x = player.getOnPos().getX();
        int z = player.getOnPos().getZ();
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        player.teleportTo(x, y, z);
        player.playSound(SoundEvents.NOTE_BLOCK_BANJO, 10, 1);

        return Command.SINGLE_SUCCESS;
    }
}
