package fun.spmc.smpmod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.fishing.FishTracker;
import fun.spmc.smpmod.fishing.FishingUtils;
import fun.spmc.smpmod.misc.NPCData;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class FishingCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("fishing").executes((ctx) -> FishTracker.openFishIndexMenu(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("setup").requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)).executes(FishingCommand::setup))
                .then(Commands.literal("kill").requires(source -> source.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)).executes(FishingCommand::kill));
    }

    private static int kill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();

        if (NPCData.get().hasNpc("fish_seller")) {
            Entity entity = NPCData.get().getMannequin(level, "fish_seller");
            if (entity != null) {
                NPCData.get().removeNpc("fish_seller");
                entity.discard();
                MessageUtils.sendSuccessMessage(ctx.getSource().getPlayerOrException(), "Fishing mannequin killed!");
                return 1;
            }

        }
        MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Fishing mannequin isn't alive!");
        return 0;
    }

    private static int setup(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        Vec3 pos = ctx.getSource().getPosition();

        if (NPCData.get().hasNpc("fish_seller")) {
            MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Fishing mannequin already exists!");
            return 0;
        }

        if (FishingUtils.spawnFishSeller(level, BlockPos.containing(pos)) == null) {
            MessageUtils.sendErrorMessage(ctx.getSource().getPlayerOrException(), "Fishing mannequin already exists!");
            return 0;
        }

        MessageUtils.sendSuccessMessage(ctx.getSource().getPlayerOrException(), "Fishing mannequin created successfully!");
        return 1;
    }
}
