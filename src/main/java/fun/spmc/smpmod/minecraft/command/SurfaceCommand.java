package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.Heightmap;

public class SurfaceCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand() {
        return CommandManager.literal("surface").executes(SurfaceCommand::surface);
    }

    public static int surface(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerWorld world = context.getSource().getWorld();

        assert player != null;

        int x = (int) Math.floor(player.getX());
        int z = (int) Math.floor(player.getZ());
        int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

        player.teleport(x, y, z, true);
        player.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 10, 1);

        return Command.SINGLE_SUCCESS;
    }
}
