package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.spmc.smpmod.minecraft.utils.CommandScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jetbrains.annotations.NotNull;

public class EnderChestCommand extends CommandScreen {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal("enderchest").executes(new EnderChestCommand());
    }
    private static final MenuConstructor SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
            ChestMenu.threeRows(syncId, inventory, player.getEnderChestInventory());

    @Override
    protected Component getScreenTitle() {
        return Component.translatable("block.minecraft.ender_chest");
    }

    @Override
    protected @NotNull MenuConstructor getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayer player) {
        player.awardStat(Stats.OPEN_ENDERCHEST, 1);
    }
}
