package a4.papers.chatfilter.chatfilter.events;


import a4.papers.chatfilter.chatfilter.ChatFilter;
import a4.papers.chatfilter.chatfilter.shared.ChatData;
import a4.papers.chatfilter.chatfilter.shared.StringSimilarity;
import a4.papers.chatfilter.chatfilter.shared.lang.EnumStrings;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatDelayListener implements EventExecutor, Listener {
    public Map<UUID, ChatData> chatmsgs = new HashMap<UUID, ChatData>();
    ChatFilter chatFilter;
    private BigDecimal similarityThreshold;

    public ChatDelayListener(ChatFilter instance) {
        chatFilter = instance;
    }
    
    private BigDecimal getSimilarityThreshold() {
        if (similarityThreshold == null) {
            String percent = chatFilter.percentage.trim().replace("%", "");
            similarityThreshold = new BigDecimal(percent).divide(BigDecimal.valueOf(100));
        }
        return similarityThreshold;
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        this.onPlayerSpam((AsyncPlayerChatEvent) event);
    }

    @EventHandler
    public void onPlayerSpam(AsyncPlayerChatEvent e) {
        if (!chatFilter.antiRepeatEnabled) {
            return;
        }
        
        Player p = e.getPlayer();
        if (p.hasPermission("chatfilter.bypass") || p.hasPermission("chatfilter.bypass.repeat")) {
            return;
        }
        
        UUID playerUUID = p.getUniqueId();
        String msg = e.getMessage();
        long currentTime = System.currentTimeMillis();
        long configtime = chatFilter.repeatDelay * 1000L;
        
        ChatData chatData = chatmsgs.get(playerUUID);
        if (chatData == null) {
            chatmsgs.put(playerUUID, new ChatData(msg, currentTime + configtime));
            return;
        }
        
        long expiryTime = chatData.getLong();
        double sim = StringSimilarity.similarity(msg, chatData.getString());
        
        if (sim > getSimilarityThreshold().doubleValue()) {
            if (expiryTime > currentTime) {
                e.setCancelled(true);
                long remainingMs = expiryTime - currentTime;
                int remainingTime = (int) Math.ceil(remainingMs / 1000.0);
                String timeString = remainingTime >= 2 ? remainingTime + " seconds" : "1 second";
                p.sendMessage(chatFilter.colour(chatFilter.getLang().mapToString(EnumStrings.chatRepeatMessage.s).replace("%time%", timeString)));
            } else {
                chatmsgs.put(playerUUID, new ChatData(msg, currentTime + configtime));
            }
        } else {
            chatmsgs.put(playerUUID, new ChatData(msg, currentTime + configtime));
        }
    }
}
