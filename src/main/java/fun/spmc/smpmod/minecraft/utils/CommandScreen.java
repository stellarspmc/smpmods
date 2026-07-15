package fun.spmc.smpmod.minecraft.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.menu.v1.FabricMenuProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuConstructor;
import org.jetbrains.annotations.NotNull;

public abstract class CommandScreen implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var senderPlayer = context.getSource().getPlayerOrException();
        senderPlayer.openMenu(createNamedScreenHandlerFactory());
        onOpen(senderPlayer);
        return 0;
    }

    protected MenuProvider createNamedScreenHandlerFactory() {
        return new SimpleMenuProvider(getScreenHandlerFactory(), getScreenTitle());
    }

    protected abstract Component getScreenTitle();

    protected abstract @NotNull MenuConstructor getScreenHandlerFactory();

    protected abstract void onOpen(ServerPlayer player);
}

