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
        StringBuffer buffer = new StringBuffer(msg.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}
