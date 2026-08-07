package fun.spmc.smpmod.economy.fluctuate;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class RotationItems {
    protected static final List<FluctuationData> temporaryItems = new ArrayList<>();

    protected static final List<FluctuationData> chosenItems = List.of(
            new FluctuationData(Items.ENCHANTED_GOLDEN_APPLE, 1500, 4.5),
            new FluctuationData(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 750, 2.5),
            new FluctuationData(Items.TOTEM_OF_UNDYING, 350, 1.5),
            new FluctuationData(Items.SHULKER_SHELL, 1200, 4)
    );

    public static void addTemporaryItem(MinecraftServer server) {
        FluctuationData data = chosenItems.get(server.overworld().getRandom().nextInt(chosenItems.size() + 1));
        temporaryItems.add(data);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            temporaryItems.forEach(data -> data.applyMarketDecay(server.overworld().getRandom()));
            addTemporaryItem(server);
        });
    }
}
