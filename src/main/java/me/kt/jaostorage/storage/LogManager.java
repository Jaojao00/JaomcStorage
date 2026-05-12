package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Ghi log mọi hành động trong kho (bán, rút, cất, chuyển).
 * Log được lưu vào SQLite để admin có thể truy vấn.
 */
public class LogManager {

    public enum Action {
        STORE,      // Cất vào kho
        WITHDRAW,   // Rút khỏi kho
        SELL,       // Bán vật phẩm
        ADMIN_ADD,  // Admin thêm
        ADMIN_REMOVE, // Admin xóa
        AUTO_STORE, // Tự động lưu khi đào
        COOP_ADD,   // Thêm coop member
        COOP_REMOVE // Xóa coop member
    }

    private final Main plugin;
    private final DatabaseManager db;

    public LogManager(Main plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    /**
     * Ghi log hành động (async).
     */
    public void log(UUID uuid, Action action, Material material, int amount, String details) {
        // Kiểm tra config có bật log cho loại hành động này không
        if (!shouldLog(action)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "INSERT INTO storage_logs (uuid, action, material, amount, details) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, action.name());
                ps.setString(3, material != null ? material.name() : null);
                ps.setInt(4, amount);
                ps.setString(5, details);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "❌ Lỗi ghi log!", e);
            }
        });
    }

    public void log(UUID uuid, Action action, Material material, int amount) {
        log(uuid, action, material, amount, null);
    }

    public void log(UUID uuid, Action action, String details) {
        log(uuid, action, null, 0, details);
    }

    private boolean shouldLog(Action action) {
        return switch (action) {
            case SELL -> plugin.getConfig().getBoolean("Log.Sales", true);
            case WITHDRAW -> plugin.getConfig().getBoolean("Log.Withdraw", true);
            case STORE, AUTO_STORE -> plugin.getConfig().getBoolean("Log.Transfer", true);
            case ADMIN_ADD, ADMIN_REMOVE -> true; // Admin actions luôn log
            case COOP_ADD, COOP_REMOVE -> true;
        };
    }

    /**
     * Xóa log cũ hơn N ngày (cleanup).
     */
    public void cleanOldLogs(int daysToKeep) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "DELETE FROM storage_logs WHERE timestamp < datetime('now', ?)")) {
                ps.setString(1, "-" + daysToKeep + " days");
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    plugin.getLogger().info("🧹 Đã xóa " + deleted + " log cũ hơn " + daysToKeep + " ngày.");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "❌ Lỗi xóa log cũ!", e);
            }
        });
    }
}
