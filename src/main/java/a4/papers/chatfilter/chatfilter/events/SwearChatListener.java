package a4.papers.chatfilter.chatfilter.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import io.papermc.paper.event.player.AsyncChatEvent;

import a4.papers.chatfilter.chatfilter.ChatFilter;
import a4.papers.chatfilter.chatfilter.shared.FilterWrapper;
import a4.papers.chatfilter.chatfilter.shared.LowerCaseReplace;
import a4.papers.chatfilter.chatfilter.shared.Result;
import a4.papers.chatfilter.chatfilter.shared.Types;
import a4.papers.chatfilter.chatfilter.shared.lang.EnumStrings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SwearChatListener implements EventExecutor, Listener {

    ChatFilter chatFilter;

    public SwearChatListener(ChatFilter instance) {
        chatFilter = instance;
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        this.onPlayerSwear((AsyncChatEvent) event);
    }


    public void onPlayerSwear(AsyncChatEvent event) {
        Player p = event.getPlayer();
        String rawMessage = chatFilter.plainMessage(event);
        String chatMessage = ChatColor.stripColor(rawMessage).toLowerCase();
        String prefix = "";
        String warnPlayerMessage =  "";
        if (p.isOp() || p.hasPermission("chatfilter.bypass") || p.hasPermission("chatfilter.bypass.chat"))
            return;
        if (event.isCancelled())
            return;
        if (chatFilter.chatPause)
            return;
        // Early check for non-English letters if enabled (after bypass/pause checks)
        if (chatFilter.settingsBlockCustomSybols) {
            String strippedRawMessage = ChatColor.stripColor(rawMessage);
            if (chatFilter.getChatFilters().containsNonEnglishLetters(strippedRawMessage)) {
                String deny = "&cYour message was not sent due to containing disallowed characters.";
                p.sendMessage(chatFilter.colour(deny));
                try {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(chatFilter.colour(deny)));
                } catch (Throwable ignored) {
                    // Fallback silently if actionbar is unavailable
                }
                event.setCancelled(true);
                return;
            }
        }
        Result result = chatFilter.getChatFilters().validResult(chatMessage, p);
        if (result.getResult()) {
            Types type = result.getType();
            String[] stringArray = result.getStringArray();
            FilterWrapper filterWrapper = result.getFilterWrapper();
            chatFilter.commandHandler.runCommand(p, stringArray, filterWrapper);
            switch (type) {
                case SWEAR:
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixChatSwear.s).replace("%player%", p.getName());
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnSwearMessage.s).replace("%placeHolder%", (chatFilter.getLang().stringArrayToString(stringArray)));
                    break;
                case IP_DNS:
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixChatIP.s).replace("%player%", p.getName());
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnIPMessage.s).replace("%placeHolder%", (chatFilter.getLang().stringArrayToString(stringArray)));
                    break;
                case IP_SWEAR:
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixChatIPandSwear.s).replace("%player%", p.getName());
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnSwearAndIPMessage.s).replace("%placeHolder%", (chatFilter.getLang().stringArrayToString(stringArray)));
                    break;
                case FONT:
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixChatFont.s).replace("%player%", p.getName());
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnFontMessage.s);
                    break;
                case URL:
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixChatIP.s).replace("%player%", p.getName());
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnURLMessage.s);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            if (filterWrapper.getLogToConsole())
                chatFilter.sendConsole(type, chatMessage, p, filterWrapper.getRegex(), "Chat");
            if (filterWrapper.getWarnPlayer())
                p.sendMessage(chatFilter.colour(warnPlayerMessage));
            if (filterWrapper.getSendStaff()) {
                String highlightColored = chatFilter.colour(chatFilter.settingsSwearHighLight);
                for (String oneWord : stringArray) {
                    chatMessage = chatMessage.replace(oneWord, highlightColored.replace("%catch%", oneWord));
                }
                chatFilter.sendStaffMessage(chatFilter.colour(prefix + chatMessage));
            }
            if (filterWrapper.getCancelChat()) {
                event.setCancelled(true);
            } else {
                String msg = rawMessage;
                for (String oneWord : stringArray) {
                    if (filterWrapper.getCancelChatReplace()) {
                       msg = LowerCaseReplace.replace(msg, oneWord, filterWrapper.getReplace());
                    }
                }
                chatFilter.setPlainMessage(event, msg);
            }
        }
    }
}
