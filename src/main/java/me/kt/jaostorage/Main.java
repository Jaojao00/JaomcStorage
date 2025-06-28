package me.kt.jaostorage;

import me.kt.jaostorage.command.AdminCommand;
import me.kt.jaostorage.command.AutoStoreCommand;
import me.kt.jaostorage.command.StorageCommand;
import me.kt.jaostorage.config.OreConfig;
import me.kt.jaostorage.gui.StorageGUI;
import me.kt.jaostorage.gui.StorageGUIListener;
import me.kt.jaostorage.listener.AutoStoreListener;
import me.kt.jaostorage.storage.SettingManager;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.storage.WhitelistManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.AudienceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private BukkitAudiences adventure;
    private OreConfig oreConfig;
    private StorageManager storageManager;
    private SettingManager settingManager;
    private WhitelistManager whitelistManager;
    private StorageGUI storageGUI;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.adventure = BukkitAudiences.create(this);

        // Load core modules
        oreConfig = new OreConfig(this);
        storageManager = new StorageManager(this);
        settingManager = new SettingManager(this);
        whitelistManager = new WhitelistManager();
        storageGUI = new StorageGUI(this);

        // Register events
        getServer().getPluginManager().registerEvents(new StorageGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new AutoStoreListener(this), this);

        // Register commands
        getCommand("kho").setExecutor(new StorageCommand(this));
        getCommand("autostore").setExecutor(new AutoStoreCommand(this));
        getCommand("jaostorage").setExecutor(new AdminCommand(this));

        getLogger().info("✅ JaomcStorage đã được bật thành công!");
    }

    @Override
    public void onDisable() {
        if (adventure != null) adventure.close();
        storageManager.saveData();
        getLogger().info("❌ JaomcStorage đã bị tắt.");
    }

    public OreConfig getOreConfig() {
        return oreConfig;
    }

    public void setOreConfig(OreConfig oreConfig) {
        this.oreConfig = oreConfig;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    public StorageGUI getStorageGUI() {
        return storageGUI;
    }

    public AudienceProvider adventure() {
        return adventure;
    }
}
