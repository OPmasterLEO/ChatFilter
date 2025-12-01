package a4.papers.chatfilter.chatfilter.events;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;

import a4.papers.chatfilter.chatfilter.ChatFilter;
import a4.papers.chatfilter.chatfilter.shared.FilterWrapper;
import a4.papers.chatfilter.chatfilter.shared.Result;
import a4.papers.chatfilter.chatfilter.shared.Types;
import a4.papers.chatfilter.chatfilter.shared.lang.EnumStrings;

public class CommandListener implements EventExecutor, Listener {

    ChatFilter chatFilter;

    public CommandListener(ChatFilter instance) {
        chatFilter = instance;
    }
    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        this.onPlayerCommand((PlayerCommandPreprocessEvent) event);
    }

    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (p.hasPermission("chatfilter.bypass") || p.hasPermission("chatfilter.bypass.command"))
            return;
        if (event.isCancelled() || !chatFilter.cmdCheck)
            return;
        
        String cmd = ChatColor.stripColor(event.getMessage().toLowerCase());
        String[] array = cmd.split(" ");
        String commandName = array[0].replace("/", "");
        
        if (chatFilter.getConfig().getConfigurationSection("commands").getKeys(false).contains(commandName)) {
            String configPath = "commands." + commandName;
            boolean swearconfig = chatFilter.getConfig().getBoolean(configPath + ".swear");
            boolean dnsconfig = chatFilter.getConfig().getBoolean(configPath + ".ip");

            String prefix = "Error";
            String warnPlayerMessage = "Error";
            Result result = chatFilter.getChatFilters().validResult(cmd, p);
            if (result.getResult()) {
                Types type = result.getType();
                String[] stringArray = result.getStringArray();
                FilterWrapper filterWrapper = result.getFilterWrapper();
                if ((type == Types.SWEAR && !swearconfig) || (type == Types.IP_DNS && !dnsconfig)) {
                    return;
                }
                
                String placeHolder = chatFilter.getLang().stringArrayToString(stringArray);
                String playerName = p.getName();
                
                if (type == Types.SWEAR && swearconfig) {
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixCmdSwear.s).replace("%player%", playerName);
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnSwearMessage.s).replace("%placeHolder%", placeHolder);
                } else if (type == Types.IP_DNS && dnsconfig) {
                    prefix = chatFilter.getLang().mapToString(EnumStrings.prefixCmdIP.s).replace("%player%", playerName);
                    warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnIPMessage.s).replace("%placeHolder%", placeHolder);
                } else if (type == Types.IP_SWEAR) {
                    if (dnsconfig && swearconfig) {
                        prefix = chatFilter.getLang().mapToString(EnumStrings.prefixCmdIPandSwear.s).replace("%player%", playerName);
                        warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnSwearAndIPMessage.s).replace("%placeHolder%", placeHolder);
                    } else if (dnsconfig) {
                        prefix = chatFilter.getLang().mapToString(EnumStrings.prefixCmdIP.s).replace("%player%", playerName);
                        warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnIPMessage.s).replace("%placeHolder%", placeHolder);
                    } else if (swearconfig) {
                        prefix = chatFilter.getLang().mapToString(EnumStrings.prefixCmdSwear.s).replace("%player%", playerName);
                        warnPlayerMessage = chatFilter.getLang().mapToString(EnumStrings.warnSwearMessage.s).replace("%placeHolder%", placeHolder);
                    }
                }
                if (filterWrapper.getCancelChat()) {
                    event.setCancelled(true);
                } else {
                    String msg = event.getMessage();
                    for (String oneWord : stringArray) {
                        if (filterWrapper.getCancelChatReplace()) {
                            msg = msg.replace(oneWord, filterWrapper.getReplace());
                        }
                    }
                    event.setMessage(msg);
                }
                chatFilter.commandHandler.runCommand(p, stringArray, filterWrapper);
                if (filterWrapper.getLogToConsole()) {
                    chatFilter.sendConsole(type, cmd, p, filterWrapper.getRegex(), "Command");
                }
                if(filterWrapper.getWarnPlayer()) {
                    p.sendMessage(chatFilter.colour(warnPlayerMessage));
                }
                if (filterWrapper.getSendStaff()) {
                    String highlightColored = chatFilter.colour(chatFilter.settingsSwearHighLight);
                    for (String oneWord : stringArray) {
                        cmd = cmd.replace(oneWord, highlightColored.replace("%catch%", oneWord));
                    }
                    chatFilter.sendStaffMessage(chatFilter.colour(prefix + cmd));
                }
            }
        }
    }
}
