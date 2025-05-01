package me.kt.jaostorage.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OreConfig {

    private final FileConfiguration config;

    public OreConfig(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "guiore.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public List<Material> getOres() {
        List<String> oreNames = config.getStringList("ores");
        List<Material> ores = new ArrayList<>();
        for (String name : oreNames) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                ores.add(mat);
            }
        }
        return ores;
    }
}
