package me.kt.jaostorage.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WhitelistManager {

    private final JavaPlugin plugin;
    private final List<Material> whitelistedItems;

    public WhitelistManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.whitelistedItems = new ArrayList<>();
        loadWhitelist();
    }

    // ✅ Tải whitelist từ config.yml
    public void loadWhitelist() {
        whitelistedItems.clear();

        FileConfiguration config = plugin.getConfig();
        List<String> itemNames = config.getStringList("Whitelist");

        for (String name : itemNames) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                whitelistedItems.add(mat);
            } else {
                plugin.getLogger().warning("❌ Vật phẩm không hợp lệ trong whitelist: " + name);
            }
        }
    }

    public List<Material> getWhitelistedItems() {
        return whitelistedItems;
    }

    public boolean isWhitelisted(Material material) {
        return whitelistedItems.contains(material);
    }
}
