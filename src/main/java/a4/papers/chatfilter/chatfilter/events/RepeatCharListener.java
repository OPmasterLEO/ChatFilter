package a4.papers.chatfilter.chatfilter.events;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import io.papermc.paper.event.player.AsyncChatEvent;

import a4.papers.chatfilter.chatfilter.ChatFilter;

public class RepeatCharListener implements EventExecutor, Listener {

    ChatFilter chatFilter;

    public RepeatCharListener(ChatFilter instance) {
        this.chatFilter = instance;
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        this.onPlayerCarSpam((AsyncChatEvent) event);
    }

    public void onPlayerCarSpam(AsyncChatEvent event) {
        String msg = chatFilter.plainMessage(event);
        if (chatFilter.antiSpamEnabled) {
            org.bukkit.entity.Player player = event.getPlayer();
            if (player.isOp() || player.hasPermission("chatfilter.bypass") || player.hasPermission("chatfilter.bypass.characters")) {
                return;
            }
            if (isURL(msg)) {
                return;
            }
            chatFilter.setPlainMessage(event, chatFilter.antiSpamPattern.matcher(msg).replaceAll(chatFilter.getAntiSpamReplacementToken()));
        }
    }

    public boolean isURL(String str) {
        return chatFilter.urlPattern.matcher(str).find();
    }
}
