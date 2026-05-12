package me.kt.jaostorage.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Quản lý danh sách quặng/vật phẩm được phép lưu trữ.
 * Đọc từ file guiore.yml.
 */
public class OreConfig {

    private final JavaPlugin plugin;
    private final List<Material> ores = new ArrayList<>();

    public OreConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        ores.clear();

        // Tạo file mặc định nếu chưa có
        File file = new File(plugin.getDataFolder(), "guiore.yml");
        if (!file.exists()) {
            plugin.saveResource("guiore.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> oreNames = config.getStringList("ores");

        for (String name : oreNames) {
            Material mat = Material.matchMaterial(name.trim());
            if (mat != null) {
                ores.add(mat);
            } else {
                plugin.getLogger().log(Level.WARNING, "⚠️ guiore.yml: Vật phẩm không hợp lệ: " + name);
            }
        }

        plugin.getLogger().info("✅ Đã tải " + ores.size() + " loại quặng từ guiore.yml");
    }

    public List<Material> getOres() {
        return ores;
    }

    public boolean isOre(Material material) {
        return ores.contains(material);
    }
}
