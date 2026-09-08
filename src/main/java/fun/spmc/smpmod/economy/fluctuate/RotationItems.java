package fun.spmc.smpmod.economy.fluctuate;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RotationItems {
    public static final List<FluctationExpiry> temporaryItems = new ArrayList<>();
    protected static final List<FluctuationData> chosenItems = List.of(
            new FluctuationData(Items.ENCHANTED_GOLDEN_APPLE, 1500, 4.5),
            new FluctuationData(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 750, 2.5),
            new FluctuationData(Items.TOTEM_OF_UNDYING, 350, 1.5),
            new FluctuationData(Items.SHULKER_SHELL, 1200, 4)
    );

    public static void addTemporaryItem(MinecraftServer server) {
        if (chosenItems.isEmpty()) return;
        FluctuationData template = chosenItems.get(server.overworld().getRandom().nextInt(chosenItems.size()));
        FluctationExpiry item = new FluctationExpiry(new FluctuationData(template.getMineral(), template.defaultPrice, template.fluctuation), server.getTickCount() + server.overworld().getRandom().nextInt(144000) + 144000);
        temporaryItems.add(item);
    }

    static int rotationTick = 144000;
    public static void serverTickLoop(MinecraftServer server) {
        int ticks = server.getTickCount();
        if (ticks == 0) return;
        if (ticks % 1200 == 0 && !temporaryItems.isEmpty()) temporaryItems.forEach(data -> data.data().applyMarketDecay(server.overworld().getRandom()));
        temporaryItems.removeIf(data -> ticks >= data.expiryTick());

        if (ticks % rotationTick == 0) {
            addTemporaryItem(server);
            rotationTick = server.overworld().getRandom().nextInt(144000) + 144000;
        }
    }

    public record FluctationExpiry(FluctuationData data, int expiryTick) {}
    public static Stream<Item> getTotalItemStream() { return Stream.concat(RotationItems.temporaryItems.stream().map((a) -> a.data().getMineral()), MarketState.getState().getAll().keySet().stream()); }
    public static Stream<Item> withDiamondStream() { return Stream.concat(getTotalItemStream(), Stream.of(Items.DIAMOND)); }
}
