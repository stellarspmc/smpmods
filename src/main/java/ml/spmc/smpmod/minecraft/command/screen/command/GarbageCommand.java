package ml.spmc.smpmod.minecraft.command.screen.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.minecraft.command.screen.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jetbrains.annotations.NotNull;

public class GarbageCommand extends Screen {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("dispose").executes(new GarbageCommand());
    }
    private static final MenuConstructor SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
            ChestMenu.sixRows(syncId, inventory);

    @Override
    protected Component getScreenTitle() {
        return Component.literal("Garbage Can");
    }

    @Override
    protected @NotNull MenuConstructor getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayer player) {
        SMPMod.SERVER.getPlayerList().broadcastSystemMessage(Component.literal("OH EM GEE " + player + " USE GARBAGE CAN MEAN " + player + " TRASH!!!!!!!!!!!!!!!"), true);
        player.awardStat(Stats.DEATHS, 100000);
    }
}
