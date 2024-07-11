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

public class ECCommand extends Screen {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal("enderchest").executes(new ECCommand());
    }
    private static final ScreenHandlerFactory SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
            GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory());

    @Override
    protected Text getScreenTitle() {
        return Text.translatable("block.minecraft.ender_chest");
    }

    @Override
    protected @NotNull ScreenHandlerFactory getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {
        player.increaseStat(Stats.OPEN_ENDERCHEST, 1);
    }
}
