package fun.spmc.smpmod.minecraft.command.screen.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.spmc.smpmod.minecraft.command.screen.Screen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class GarbageCommand extends Screen {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("dispose").executes(new GarbageCommand());
    }
    private static final ScreenHandlerFactory SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
            GenericContainerScreenHandler.createGeneric9x6(syncId, inventory);

    @Override
    protected Text getScreenTitle() {
        return Text.literal("Garbage Can");
    }

    @Override
    protected @NotNull ScreenHandlerFactory getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {
        player.increaseStat(Stats.DEATHS, 100000);
    }
}
