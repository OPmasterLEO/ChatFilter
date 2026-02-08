package a4.papers.chatfilter.chatfilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Manager {

    ChatFilter chatFilter;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");

    public Manager(ChatFilter instance) {
        chatFilter = instance;
    }

    public static final char COLOR_CHAR = '\u00A7';

    public boolean supported(String string) {
        boolean statement = false;
        switch (string.toLowerCase()) {
            case "hex":
                statement = Integer.parseInt(Bukkit.getBukkitVersion().split("[.\\-]")[1]) >= 16;
                break;
            case "text-component":
                statement = Integer.parseInt(Bukkit.getBukkitVersion().split("[.\\-]")[1]) >= 12;
                break;
        }
        return statement;
    }

    public static String colorStringHex(String msg) {
        Matcher matcher = HEX_PATTERN.matcher(msg);
        StringBuilder buffer = new StringBuilder(msg.length() + 4 * 8);
        int lastEnd = 0;
        while (matcher.find()) {
            buffer.append(msg, lastEnd, matcher.start());
            String group = matcher.group(1);
            buffer.append(COLOR_CHAR).append("x")
                    .append(COLOR_CHAR).append(group.charAt(0)).append(COLOR_CHAR).append(group.charAt(1))
                    .append(COLOR_CHAR).append(group.charAt(2)).append(COLOR_CHAR).append(group.charAt(3))
                    .append(COLOR_CHAR).append(group.charAt(4)).append(COLOR_CHAR).append(group.charAt(5));
            lastEnd = matcher.end();
        }
        buffer.append(msg.substring(lastEnd));
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
