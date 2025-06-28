package me.kt.jaostorage.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

public class JaoPlaceholderExpansion extends PlaceholderExpansion {

    private final Main plugin;
    private final StorageManager storageManager;

    public JaoPlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    @Override
    public String getIdentifier() {
        return "jaostorage";
    }

    @Override
    public String getAuthor() {
        return "TaiNguyen";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // giữ placeholder sau reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.hasPlayedBefore()) return "";

        if (params.equalsIgnoreCase("slot")) {
            return String.valueOf(storageManager.getSlots(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("infinity")) {
            return String.valueOf(storageManager.isInfinity(player.getUniqueId()));
        }

        if (params.startsWith("amount_")) {
            String item = params.substring("amount_".length()).toUpperCase();
            Material material = Material.matchMaterial(item);
            if (material != null) {
                int amount = storageManager.getAmount(player.getUniqueId(), material);
                return String.valueOf(amount);
            }
        }

        return null;
    }
}