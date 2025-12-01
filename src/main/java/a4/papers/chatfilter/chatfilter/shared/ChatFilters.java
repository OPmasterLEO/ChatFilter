package a4.papers.chatfilter.chatfilter.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import a4.papers.chatfilter.chatfilter.ChatFilter;

public class ChatFilters {

    ChatFilter chatFilter;

    public ChatFilters(ChatFilter instance) {
        chatFilter = instance;
    }

    private String removeBypass(String s) {
        for (String removewording : chatFilter.byPassWords) {
            if (s.contains(removewording)) {
                s = s.replace(removewording, " ");
            }
        }
        for (String removewording : chatFilter.byPassDNS) {
            if (s.contains(removewording)) {
                s = s.replace(removewording, " ");
            }
        }
        return s;
    }

    public Result validResult(String string, Player player) {
        String lowercaseString = removeBypass(string.toLowerCase());
        
        boolean matched = false;
        boolean matchedSwear = false;
        boolean matchedIP = false;
        boolean matchedURL = false;
        String regex = "";
        boolean canBypassSwear = player.hasPermission("chatfilter.bypass.swear");
        boolean canBypassIP = player.hasPermission("chatfilter.bypass.ip");
        boolean canBypassURL = player.hasPermission("chatfilter.bypass.url");
        
        List<String> groupWords = new ArrayList<>();
        List<String> regexUsed = new ArrayList<>();
        
        // Check swear words
        if (!canBypassSwear && !chatFilter.wordRegexPattern.isEmpty()) {
            for (Pattern p : chatFilter.wordRegexPattern) {
                Matcher m = p.matcher(lowercaseString);
                while (m.find()) {
                    String match = m.group(0);
                    if (!player.hasPermission("chatfilter.bypass.swear." + match)) {
                        matched = true;
                        matchedSwear = true;
                        regex = p.pattern();
                        regexUsed.add(regex);
                        if (!groupWords.contains(match)) {
                            groupWords.add(match);
                        }
                    }
                }
            }
        }

        if (!canBypassIP && !chatFilter.advertRegexPattern.isEmpty()) {
            for (Pattern p : chatFilter.advertRegexPattern) {
                Matcher m = p.matcher(lowercaseString);
                while (m.find()) {
                    String match = m.group(0);
                    if (!player.hasPermission("chatfilter.bypass.ip." + match)) {
                        matched = true;
                        matchedIP = true;
                        regex = p.pattern();
                        if (!groupWords.contains(match)) {
                            groupWords.add(match);
                        }
                    }
                }
            }
        }

        // Build regex map only when needed
        Map<String, FilterWrapper> regexMap = new HashMap<>();
        
        // Check URL
        if (!canBypassURL && !chatFilter.settingsAllowURL) {
            Matcher m = chatFilter.urlPattern.matcher(lowercaseString);
            if (m.find()) {
                matched = true;
                matchedURL = true;
                regex = chatFilter.URL_REGEX;
                regexMap.put(chatFilter.URL_REGEX, new FilterWrapper("URL", Collections.singletonList("none"), chatFilter.URL_REGEX, true, false, "", false, true, false));
            }
        }

        // Check font
        if (isFont(string)) {
            matched = true;
            regex = "unicode";
            regexMap.put("unicode", new FilterWrapper("unicode", Collections.singletonList("none"), "unicode", true, false, "", true, true, true));
        }
        
        // Determine type
        Types type;
        if (matchedSwear && matchedIP) {
            type = Types.IP_SWEAR;
        } else if (matchedSwear) {
            type = Types.SWEAR;
        } else if (matchedIP) {
            type = Types.IP_DNS;
        } else if (matchedURL) {
            type = Types.URL;
        } else if (matched) {
            type = Types.FONT;
        } else {
            type = Types.NOTYPE;
        }

        String[] array = groupWords.toArray(new String[groupWords.size()]);
        regexMap.putAll(chatFilter.regexWords);
        regexMap.putAll(chatFilter.regexAdvert);
        return new Result(matched, array, type, regexMap.get(regex), regexUsed);
    }

    public boolean isFont(String string) {
        if (!chatFilter.settingsBlockFancyChat || chatFilter.unicodeBlacklist.isEmpty()) {
            return false;
        }
        
        // Remove whitelisted characters first
        String processedString = string;
        for (String s : chatFilter.unicodeWhitelist) {
            if (processedString.contains(s)) {
                processedString = processedString.replace(s, "");
            }
        }
        
        // Early exit if string is now empty
        if (processedString.isEmpty()) {
            return false;
        }
        
        // Check unicode ranges
        for (UnicodeWrapper wrapper : chatFilter.unicodeBlacklist.values()) {
            int urangeLow = wrapper.getStartInt();
            int urangeHigh = wrapper.getEndInt();
            
            int length = processedString.length();
            for (int i = 0; i < length; ) {
                int cp = processedString.codePointAt(i);
                if (cp >= urangeLow && cp <= urangeHigh) {
                    return true;
                }
                i += Character.charCount(cp);
            }
        }
        return false;
    }
}
