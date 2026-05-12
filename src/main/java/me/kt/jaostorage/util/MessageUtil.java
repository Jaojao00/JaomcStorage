package me.kt.jaostorage.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * Tiện ích gửi tin nhắn, format số, và color codes.
 */
public final class MessageUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");
    private static final String PREFIX = "§6§l[JaoStorage] §r";

    private MessageUtil() {}

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void send(Player player, String message) {
        player.sendMessage(PREFIX + color(message));
    }

    public static void sendRaw(Player player, String message) {
        player.sendMessage(color(message));
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(color(message)));
    }

    public static String formatNumber(int number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatNumber(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatMaterial(String materialName) {
        return materialName.replace("_", " ").toLowerCase();
    }
}
