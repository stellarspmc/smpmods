package ml.spmc.smpmod.minecraft.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

public interface PlayerLeaveCallback {
    Event<PlayerLeaveCallback> EVENT = EventFactory.createArrayBacked(PlayerLeaveCallback.class, callbacks -> playerEntity -> {
        for (PlayerLeaveCallback callback : callbacks) {
            callback.onLeave(playerEntity);
        }
    });

    void onLeave(Player playerEntity);
}
