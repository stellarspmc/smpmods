package fun.spmc.smpmod;

import fun.spmc.smpmod.utils.UtilClass;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static fun.spmc.smpmod.SMPMod.*;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getChannel() != messageChannel || e.getAuthor().isBot() || minecraftServer == null) return;
        if (e.getMessage().getAttachments().isEmpty() && e.getMessage().getStickers().isEmpty()) UtilClass.broadcastMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
        else if (!e.getMessage().getStickers().isEmpty() && e.getMessage().getAttachments().isEmpty()) UtilClass.broadcastMessage(e.getAuthor().getName(), "<sticker>");
        else UtilClass.broadcastMessage(e.getAuthor().getName(), "<attachment>");
    }
}