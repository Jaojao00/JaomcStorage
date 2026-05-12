package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.database.DatabaseManager;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Quản lý cài đặt cá nhân của người chơi (autoStore, etc.)
 * Sử dụng cache + SQLite persistence.
 */
public class SettingManager {

    private final Main plugin;
    private final DatabaseManager db;
    private final ConcurrentHashMap<UUID, Boolean> autoStoreCache = new ConcurrentHashMap<>();

    public SettingManager(Main plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        loadAll();
    }

    private void loadAll() {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT uuid, auto_store FROM player_settings");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                boolean autoStore = rs.getInt("auto_store") == 1;
                autoStoreCache.put(uuid, autoStore);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Không thể tải player_settings!", e);
        }
    }

    // ==================== AUTO STORE ====================

    public boolean isAutoStoreEnabled(Player player) {
        return isAutoStoreEnabled(player.getUniqueId());
    }

    public boolean isAutoStoreEnabled(UUID uuid) {
        return autoStoreCache.getOrDefault(uuid, false);
    }

    public void setAutoStore(Player player, boolean enabled) {
        setAutoStore(player.getUniqueId(), enabled);
    }

    public void setAutoStore(UUID uuid, boolean enabled) {
        autoStoreCache.put(uuid, enabled);

        // Async save
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "INSERT OR REPLACE INTO player_settings (uuid, auto_store, upgraded_slots, infinity) " +
                    "VALUES (?, ?, " +
                    "COALESCE((SELECT upgraded_slots FROM player_settings WHERE uuid = ?), 0), " +
                    "COALESCE((SELECT infinity FROM player_settings WHERE uuid = ?), 0))")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, enabled ? 1 : 0);
                ps.setString(3, uuid.toString());
                ps.setString(4, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "❌ Lỗi lưu autoStore!", e);
            }
        });
    }

    public boolean toggleAutoStore(Player player) {
        boolean newState = !isAutoStoreEnabled(player);
        setAutoStore(player, newState);
        return newState;
    }

    // ==================== CONFIG HELPERS ====================

    public boolean getBoolean(String path) {
        return plugin.getConfig().getBoolean(path, false);
    }

    public String getString(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public java.util.List<String> getStringList(String path) {
        return plugin.getConfig().getStringList(path);
    }

    public int getInt(String path) {
        return plugin.getConfig().getInt(path, 0);
    }

    public double getDouble(String path) {
        return plugin.getConfig().getDouble(path, 0.0);
    }
}