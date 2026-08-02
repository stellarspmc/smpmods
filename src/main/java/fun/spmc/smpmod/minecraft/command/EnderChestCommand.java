package fun.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

public class EnderChestCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("enderchest")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.openMenu(new SimpleMenuProvider((syncId, inventory, p) ->
                            ChestMenu.threeRows(syncId, inventory, p.getEnderChestInventory()), Component.translatable("block.minecraft.ender_chest")
                    ));
                    player.awardStat(Stats.OPEN_ENDERCHEST, 1);
                    return 1;
                });
    }
}
