package me.kt.jaostorage;

import me.kt.jaostorage.command.AdminCommand;
import me.kt.jaostorage.command.AutoStoreCommand;
import me.kt.jaostorage.command.StorageCommand;
import me.kt.jaostorage.config.OreConfig;
import me.kt.jaostorage.database.DatabaseManager;
import me.kt.jaostorage.gui.StorageGUI;
import me.kt.jaostorage.gui.StorageGUIListener;
import me.kt.jaostorage.listener.AutoStoreListener;
import me.kt.jaostorage.placeholder.JaoPlaceholderExpansion;
import me.kt.jaostorage.storage.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin chính JaomcStorage 2.0
 * Kho lưu trữ khoáng sản nâng cấp cho server Skyblock.
 *
 * @author Tài Nguyễn
 * @version 2.0
 */
public class Main extends JavaPlugin {

    private static Main instance;

    // Core modules
    private DatabaseManager databaseManager;
    private OreConfig oreConfig;
    private StorageManager storageManager;
    private SettingManager settingManager;
    private CoopManager coopManager;
    private EconomyManager economyManager;
    private LogManager logManager;
    private StorageGUI storageGUI;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        // 1. Lưu config mặc định
        saveDefaultConfig();

        // 2. Khởi tạo Database (SQLite)
        databaseManager = new DatabaseManager(this);

        // 3. Khởi tạo Config
        oreConfig = new OreConfig(this);

        // 4. Khởi tạo Economy (Vault)
        economyManager = new EconomyManager(this);

        // 5. Khởi tạo Log Manager
        logManager = new LogManager(this, databaseManager);

        // 6. Khởi tạo Core Managers
        storageManager = new StorageManager(this, databaseManager);
        settingManager = new SettingManager(this, databaseManager);
        coopManager = new CoopManager(this, databaseManager);

        // 7. Khởi tạo GUI
        storageGUI = new StorageGUI(this);

        // 8. Đăng ký Events
        getServer().getPluginManager().registerEvents(new StorageGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new AutoStoreListener(this), this);

        // 9. Đăng ký Commands
        var khoCmd = getCommand("kho");
        if (khoCmd != null) {
            StorageCommand storageCmd = new StorageCommand(this);
            khoCmd.setExecutor(storageCmd);
            khoCmd.setTabCompleter(storageCmd);
        }

        var autoStoreCmd = getCommand("autostore");
        if (autoStoreCmd != null) {
            autoStoreCmd.setExecutor(new AutoStoreCommand(this));
        }

        var adminCmd = getCommand("jaostorage");
        if (adminCmd != null) {
            AdminCommand admin = new AdminCommand(this);
            adminCmd.setExecutor(admin);
            adminCmd.setTabCompleter(admin);
        }

        // 10. Đăng ký PlaceholderAPI (nếu có)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new JaoPlaceholderExpansion(this).register();
            getLogger().info("✅ Đã đăng ký PlaceholderAPI expansion!");
        }

        // 11. Cleanup log cũ (chạy async sau 1 phút)
        int logRetentionDays = getConfig().getInt("Log.RetentionDays", 30);
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () ->
            logManager.cleanOldLogs(logRetentionDays), 20L * 60);

        long elapsed = System.currentTimeMillis() - startTime;
        getLogger().info("═══════════════════════════════════════");
        getLogger().info("  💎 JaomcStorage 2.0 đã bật thành công!");
        getLogger().info("  ⏱️ Thời gian khởi tạo: " + elapsed + "ms");
        getLogger().info("  💾 Database: SQLite");
        getLogger().info("  💰 Economy: " + (economyManager.isEnabled() ? "✅ " + "Vault" : "❌ Chưa cài"));
        getLogger().info("  📊 PlaceholderAPI: " + (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null ? "✅" : "❌"));
        getLogger().info("═══════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        // Lưu toàn bộ dữ liệu
        if (storageManager != null) {
            storageManager.shutdown();
        }

        // Đóng database
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("❌ JaomcStorage đã tắt. Dữ liệu đã được lưu.");
    }

    // ==================== GETTERS ====================

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public OreConfig getOreConfig() { return oreConfig; }
    public void setOreConfig(OreConfig oreConfig) { this.oreConfig = oreConfig; }
    public StorageManager getStorageManager() { return storageManager; }
    public SettingManager getSettingManager() { return settingManager; }
    public CoopManager getCoopManager() { return coopManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public LogManager getLogManager() { return logManager; }
    public StorageGUI getStorageGUI() { return storageGUI; }
}
