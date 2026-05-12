package me.kt.jaostorage.database;

import me.kt.jaostorage.Main;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

/**
 * Quản lý kết nối SQLite và khởi tạo schema database.
 * Sử dụng single connection với auto-reconnect.
 */
public class DatabaseManager {

    private final Main plugin;
    private Connection connection;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "storage.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            // Tối ưu SQLite cho performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA cache_size=10000");
            }

            plugin.getLogger().info("✅ Kết nối SQLite thành công!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Không thể kết nối SQLite!", e);
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Bảng lưu trữ vật phẩm
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_storage (" +
                "uuid TEXT NOT NULL, " +
                "material TEXT NOT NULL, " +
                "amount INTEGER DEFAULT 0, " +
                "PRIMARY KEY (uuid, material))"
            );

            // Bảng cài đặt người chơi
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_settings (" +
                "uuid TEXT PRIMARY KEY, " +
                "auto_store INTEGER DEFAULT 0, " +
                "upgraded_slots INTEGER DEFAULT 0, " +
                "infinity INTEGER DEFAULT 0)"
            );

            // Bảng chia sẻ kho (Coop)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS coop_members (" +
                "owner_uuid TEXT NOT NULL, " +
                "member_uuid TEXT NOT NULL, " +
                "added_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (owner_uuid, member_uuid))"
            );

            // Bảng log hành động
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS storage_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT NOT NULL, " +
                "action TEXT NOT NULL, " +
                "material TEXT, " +
                "amount INTEGER DEFAULT 0, " +
                "details TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"
            );

            // Indexes
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_storage_uuid ON player_storage(uuid)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_logs_uuid ON storage_logs(uuid)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_coop_owner ON coop_members(owner_uuid)");

            plugin.getLogger().info("✅ Khởi tạo bảng dữ liệu thành công!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Không thể tạo bảng!", e);
        }
    }

    /**
     * Lấy connection với auto-reconnect nếu bị đóng.
     */
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    /**
     * Đóng kết nối database an toàn.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("✅ Đã đóng kết nối SQLite.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "❌ Lỗi khi đóng SQLite!", e);
        }
    }

    /**
     * Migrate dữ liệu từ file YAML cũ sang SQLite.
     */
    public void migrateFromYaml(File yamlFile) {
        if (!yamlFile.exists()) return;

        plugin.getLogger().info("🔄 Phát hiện storage.yml cũ, bắt đầu migrate sang SQLite...");
        org.bukkit.configuration.file.FileConfiguration yaml =
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(yamlFile);

        try {
            connection.setAutoCommit(false);

            // Migrate storage data
            if (yaml.contains("data")) {
                for (String uuidStr : yaml.getConfigurationSection("data").getKeys(false)) {
                    for (String matName : yaml.getConfigurationSection("data." + uuidStr).getKeys(false)) {
                        int amount = yaml.getInt("data." + uuidStr + "." + matName);
                        try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT OR REPLACE INTO player_storage (uuid, material, amount) VALUES (?, ?, ?)")) {
                            ps.setString(1, uuidStr);
                            ps.setString(2, matName);
                            ps.setInt(3, amount);
                            ps.executeUpdate();
                        }
                    }
                }
            }

            // Migrate slots
            if (yaml.contains("slots")) {
                for (String uuidStr : yaml.getConfigurationSection("slots").getKeys(false)) {
                    int slots = yaml.getInt("slots." + uuidStr);
                    try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT OR REPLACE INTO player_settings (uuid, upgraded_slots) VALUES (?, ?)")) {
                        ps.setString(1, uuidStr);
                        ps.setInt(2, slots);
                        ps.executeUpdate();
                    }
                }
            }

            // Migrate infinity
            if (yaml.contains("infinity")) {
                for (String uuidStr : yaml.getStringList("infinity")) {
                    try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT OR REPLACE INTO player_settings (uuid, infinity) VALUES (?, 1)")) {
                        ps.setString(1, uuidStr);
                        ps.executeUpdate();
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);

            // Rename old file
            File backup = new File(yamlFile.getParent(), "storage.yml.migrated");
            yamlFile.renameTo(backup);

            plugin.getLogger().info("✅ Migrate thành công! File cũ đã đổi tên thành storage.yml.migrated");
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            plugin.getLogger().log(Level.SEVERE, "❌ Lỗi khi migrate dữ liệu!", e);
        }
    }
}
