package ml.spmc.smpmod.minecraft.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, callbacks -> (playerEntity, damageSource) -> {
        for (PlayerDeathCallback callback : callbacks) {
            callback.onPlayerDeath(playerEntity, damageSource);
        }
    });

    void onPlayerDeath(Player playerEntity, Component text);
}
