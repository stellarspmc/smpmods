package ml.spmc.smpmod.minecraft.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

public interface PlayerJoinCallback {
    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class, callbacks -> (connection, playerEntity) -> {
        for (PlayerJoinCallback callback : callbacks) {
            callback.onJoin(connection, playerEntity);
        }
    });

    void onJoin(Connection connection, Player playerEntity);
}
