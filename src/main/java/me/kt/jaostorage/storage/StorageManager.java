package me.kt.jaostorage.storage;

import java.io.File;
import java.io.IOException;
import java.util.*;

import me.kt.jaostorage.Main;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class StorageManager {

    private final Map<UUID, Map<Material, Integer>> storageData = new HashMap<>();
    private final Map<UUID, Integer> storageSlots = new HashMap<>();
    private final Set<UUID> infinityMode = new HashSet<>();
    private final Main plugin;

    private File dataFile;
    private FileConfiguration dataConfig;

    public StorageManager(Main plugin) {
        this.plugin = plugin;

        dataFile = new File(plugin.getDataFolder(), "storage.yml");
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    plugin.getLogger().info("Đã tạo mới file storage.yml");
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Không thể tạo storage.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadData();
    }

    // ========== SLOT LƯU TRỮ ==========
    public void upgradeStorage(Player player, int slots) {
        upgradeStorage(player.getUniqueId(), slots);
    }

    public void upgradeStorage(UUID uuid, int slots) {
        storageSlots.put(uuid, getSlots(uuid) + slots);
        saveData();
    }

    public int getSlots(Player player) {
        return getSlots(player.getUniqueId());
    }

    public int getSlots(UUID uuid) {
        return storageSlots.getOrDefault(uuid, 0);
    }

    // ========== KHO VÔ HẠN ==========
    public void setInfinity(Player player, boolean enabled) {
        setInfinity(player.getUniqueId(), enabled);
    }

    public void setInfinity(UUID uuid, boolean enabled) {
        if (enabled) infinityMode.add(uuid);
        else infinityMode.remove(uuid);
        saveData();
    }

    public boolean isInfinity(Player player) {
        return isInfinity(player.getUniqueId());
    }

    public boolean isInfinity(UUID uuid) {
        return infinityMode.contains(uuid);
    }

    public boolean toggleInfinity(UUID uuid) {
        boolean newState = !infinityMode.contains(uuid);
        setInfinity(uuid, newState);
        return newState;
    }

    // ========== LƯU TRỮ VẬT PHẨM ==========
    public int getAmount(Player player, Material material) {
        return getAmount(player.getUniqueId(), material);
    }

    public int getAmount(UUID uuid, Material material) {
        return storageData.computeIfAbsent(uuid, k -> new HashMap<>()).getOrDefault(material, 0);
    }

    public void setAmount(UUID uuid, Material material, int amount) {
        storageData.computeIfAbsent(uuid, k -> new HashMap<>()).put(material, amount);
        saveData();
    }

    public void addItem(UUID uuid, Material material, int amount) {
        int current = getAmount(uuid, material);
        setAmount(uuid, material, current + amount);
    }

    public boolean removeItem(UUID uuid, Material material, int amount) {
        int current = getAmount(uuid, material);
        if (current < amount) return false;
        setAmount(uuid, material, current - amount);
        return true;
    }

    public Map<Material, Integer> getStorage(Player player) {
        return getStorage(player.getUniqueId());
    }

    public Map<Material, Integer> getStorage(UUID uuid) {
        return storageData.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    public boolean isStorable(Material material) {
        return plugin.getOreConfig().getOres().contains(material);
    }

    // ========== LƯU FILE ==========
    public void saveData() {
        dataConfig.set("data", null);
        dataConfig.set("slots", null);
        dataConfig.set("infinity", null);

        for (UUID uuid : storageData.keySet()) {
            for (Map.Entry<Material, Integer> entry : storageData.get(uuid).entrySet()) {
                dataConfig.set("data." + uuid + "." + entry.getKey().name(), entry.getValue());
            }
        }

        for (UUID uuid : storageSlots.keySet()) {
            dataConfig.set("slots." + uuid, storageSlots.get(uuid));
        }

        List<String> infinityList = new ArrayList<>();
        for (UUID uuid : infinityMode) {
            infinityList.add(uuid.toString());
        }
        dataConfig.set("infinity", infinityList);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Không thể lưu storage.yml: " + e.getMessage());
        }
    }

    // ========== TẢI FILE ==========
    public void loadData() {
        if (dataConfig.contains("data")) {
            for (String uuidStr : dataConfig.getConfigurationSection("data").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<Material, Integer> items = new HashMap<>();
                for (String matName : dataConfig.getConfigurationSection("data." + uuidStr).getKeys(false)) {
                    Material mat = Material.getMaterial(matName);
                    int amount = dataConfig.getInt("data." + uuidStr + "." + matName);
                    if (mat != null) items.put(mat, amount);
                }
                storageData.put(uuid, items);
            }
        }

        if (dataConfig.contains("slots")) {
            for (String uuidStr : dataConfig.getConfigurationSection("slots").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                int slots = dataConfig.getInt("slots." + uuidStr);
                storageSlots.put(uuid, slots);
            }
        }

        if (dataConfig.contains("infinity")) {
            for (String uuidStr : dataConfig.getStringList("infinity")) {
                infinityMode.add(UUID.fromString(uuidStr));
            }
        }
    }

    public void addItemToPlayerStorage(UUID uniqueId, Material mat, int amount) {
        if (isStorable(mat)) {
            addItem(uniqueId, mat, amount);
        } else {
            plugin.getLogger().warning("Vật phẩm " + mat.name() + " không thể lưu trữ.");
        }
    }

    public void removeItemFromPlayerStorage(UUID uniqueId, Material mat, int amount) {
        if (isStorable(mat)) {
            removeItem(uniqueId, mat, amount);
        } else {
            plugin.getLogger().warning("Vật phẩm " + mat.name() + " không thể lưu trữ.");
        }
    }
}