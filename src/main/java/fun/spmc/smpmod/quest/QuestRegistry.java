package fun.spmc.smpmod.quest;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class QuestRegistry {
    protected static void generateRandomRegistry() { // test method

    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((level, player, blockPos, blockState, blockEntity) -> {

        });
    }
}
