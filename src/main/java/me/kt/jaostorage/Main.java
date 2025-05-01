package me.kt.jaostorage;

import me.kt.jaostorage.command.AdminCommand;
import me.kt.jaostorage.command.AutoStoreCommand;
import me.kt.jaostorage.command.StorageCommand;
import me.kt.jaostorage.config.OreConfig;
import me.kt.jaostorage.config.WhitelistManager;
import me.kt.jaostorage.listeners.AutoStoreListener;
import me.kt.jaostorage.listeners.BlockBreakListener;
import me.kt.jaostorage.gui.StorageGUI;
import me.kt.jaostorage.gui.StorageGUIListener;
import me.kt.jaostorage.placeholder.JaoPlaceholderExpansion;
import me.kt.jaostorage.storage.CoopManager;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.storage.SettingManager;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private StorageManager storageManager;
    private CoopManager coopManager;
    private StorageGUI storageGUI;
    private OreConfig oreConfig;
    private SettingManager settingManager;
    private WhitelistManager whitelistManager;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;

        // ✅ 1. Load config mặc định
        saveDefaultConfig();
        saveResource("guiore.yml", false);

        // ✅ 2. Khởi tạo các class quan trọng
        oreConfig = new OreConfig(this);
        whitelistManager = new WhitelistManager(this);
        storageManager = new StorageManager(this); // ⚠️ KHỞI TẠO TRƯỚC KHI DÙNG
        coopManager = new CoopManager();
        storageGUI = new StorageGUI(this);
        settingManager = new SettingManager(this);

        // ✅ 3. Khởi tạo Adventure API
        this.adventure = BukkitAudiences.create(this);

        // ✅ 4. (Tuỳ chọn) Lưu dữ liệu kho ngay khi bật nếu cần
        // storageManager.saveData(); // ❌ KHÔNG GỌI Ở ĐÂY nếu chưa có dữ liệu

        // ✅ 5. Đăng ký lệnh
        getCommand("jaostorage").setExecutor(new AdminCommand(this));
        getCommand("kho").setExecutor(new StorageCommand(this));
        getCommand("autostore").setExecutor(new AutoStoreCommand(this));

        // ✅ 6. Đăng ký sự kiện
        getServer().getPluginManager().registerEvents(new BlockBreakListener(storageManager, whitelistManager.getWhitelistedItems()), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(storageManager, oreConfig.getOres()), this);
        getServer().getPluginManager().registerEvents(new StorageGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new AutoStoreListener(this), this);

        // ✅ 7. Tích hợp PlaceholderAPI nếu có
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new JaoPlaceholderExpansion(this).register();
        }

        getLogger().info("✅ JaoStorage đã bật thành công!");
    }

    @Override
    public void onDisable() {
        // ✅ Lưu dữ liệu kho trước khi tắt plugin
        if (storageManager != null) {
            storageManager.saveData();
        }

        // ✅ Đóng Adventure API nếu có
        if (this.adventure != null) {
            this.adventure.close();
        }

        getLogger().info("⛔ JaoStorage đã tắt!");
    }

    // ======================
    // 🔧 Getter cho plugin
    // ======================

    public static Main getInstance() {
        return instance;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public CoopManager getCoopManager() {
        return coopManager;
    }

    public StorageGUI getStorageGUI() {
        return storageGUI;
    }

    public OreConfig getOreConfig() {
        return oreConfig;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public void setOreConfig(OreConfig oreConfig) {
        this.oreConfig = oreConfig;
    }

    public BukkitAudiences adventure() {
        return this.adventure;
    }
}
