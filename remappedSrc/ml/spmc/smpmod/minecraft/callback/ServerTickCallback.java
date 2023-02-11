package ml.spmc.smpmod.minecraft.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ServerTickCallback {
    Event<ServerTickCallback> EVENT = EventFactory.createArrayBacked(ServerTickCallback.class, callbacks -> {
        for (ServerTickCallback callback : callbacks) {
            callback.onTick();
        }
        return null;
    });

    void onTick();
}
