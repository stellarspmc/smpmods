package ml.spmc.smpmod.minecraft.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface ServerChatCallback {
    Event<ServerChatCallback> EVENT = EventFactory.createArrayBacked(ServerChatCallback.class, callbacks -> (playerEntity, rawMessage) -> {
        Optional<Component> msg = Optional.empty();
        for (ServerChatCallback callback : callbacks) {
            Optional<Component> callbackResult = callback.onServerChat(playerEntity, rawMessage);
            if (callbackResult.isPresent()) msg = callbackResult;
        }
        return msg;
    });

    Optional<Component> onServerChat(Player playerEntity, String rawMessage);
}
