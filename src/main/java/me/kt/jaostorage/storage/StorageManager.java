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

    // 🔐 Dữ liệu vật phẩm được lưu theo UUID của người chơi
    private final Map<UUID, Map<Material, Integer>> storageData = new HashMap<>();
    private final Map<UUID, Map<Material, Integer>> playerStorage = new HashMap<>();

    // 📦 Số slot lưu trữ mở rộng (dành cho nâng cấp dung lượng)
    private final Map<UUID, Integer> storageSlots = new HashMap<>();

    // ♾️ Danh sách người chơi có chế độ kho vô hạn
    private final Set<UUID> infinityMode = new HashSet<>();

    // 🚧 Giới hạn lưu trữ tối đa theo từng người chơi (nâng cấp theo cấp độ)
    private final Map<UUID, Integer> storageLimit = new HashMap<>();

    // 🔁 Plugin chính, để dùng khi cần truy cập config, file,...
    private final Main plugin;

    // 📁 File YAML lưu dữ liệu
    private File dataFile;
    private FileConfiguration dataConfig;

    // ✅ Constructor nhận plugin chính và load file dữ liệu
    public StorageManager(Main plugin) {
        this.plugin = plugin;

        // 🔄 Tạo file YAML để lưu trữ dữ liệu kho
        dataFile = new File(plugin.getDataFolder(), "databeas.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        loadData(); // 🔁 Tải dữ liệu từ file khi khởi động
    }

    // ==============================
    // 📦 PHẦN QUẢN LÝ SLOT LƯU TRỮ
    // ==============================

    public void upgradeStorage(Player player, int slots) {
        upgradeStorage(player.getUniqueId(), slots);
    }

    public void upgradeStorage(UUID uuid, int slots) {
        storageSlots.put(uuid, getSlots(uuid) + slots);
    }

    public int getSlots(Player player) {
        return getSlots(player.getUniqueId());
    }

    public int getSlots(UUID uuid) {
        return storageSlots.getOrDefault(uuid, 0);
    }

    // ==============================
    // ♾️ PHẦN QUẢN LÝ KHO VÔ HẠN
    // ==============================

    public void setInfinity(Player player, boolean enabled) {
        setInfinity(player.getUniqueId(), enabled);
    }

    public void setInfinity(UUID uuid, boolean enabled) {
        if (enabled) {
            infinityMode.add(uuid);
        } else {
            infinityMode.remove(uuid);
        }
    }

    public boolean isInfinity(Player player) {
        return isInfinity(player.getUniqueId());
    }

    public boolean isInfinity(UUID uuid) {
        return infinityMode.contains(uuid);
    }

    public boolean toggleInfinity(UUID uuid) {
        if (infinityMode.contains(uuid)) {
            infinityMode.remove(uuid);
            return false;
        } else {
            infinityMode.add(uuid);
            return true;
        }
    }

    // ==============================
    // 📥📤 PHẦN QUẢN LÝ VẬT PHẨM
    // ==============================

    public int getAmount(Player player, Material material) {
        return getAmount(player.getUniqueId(), material);
    }

    public int getAmount(UUID uuid, Material material) {
        return storageData.computeIfAbsent(uuid, k -> new HashMap<>()).getOrDefault(material, 0);
    }

    public void setAmount(Player player, Material material, int amount) {
        setAmount(player.getUniqueId(), material, amount);
    }

    public void setAmount(UUID uuid, Material material, int amount) {
        storageData.computeIfAbsent(uuid, k -> new HashMap<>()).put(material, amount);
    }

    public void addItem(Player player, Material material, int amount) {
        addItem(player.getUniqueId(), material, amount);
    }

    public void addItem(UUID uuid, Material material, int amount) {
        int current = getAmount(uuid, material);
        setAmount(uuid, material, current + amount);
    }

    public boolean removeItem(Player player, Material material, int amount) {
        return removeItem(player.getUniqueId(), material, amount);
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
        return storageData.getOrDefault(uuid, new HashMap<>());
    }

    public void addStorageSlots(UUID uuid, int slots) {
        int current = storageLimit.getOrDefault(uuid, 0);
        storageLimit.put(uuid, current + slots);
    }

    // ✅ Dùng để kiểm tra xem loại item đó có được lưu vào kho không
    public boolean isStorable(Material material) {
        return plugin.getOreConfig().getOres().contains(material);
    }

    public boolean hasStorage(UUID uuid) {
        return playerStorage.containsKey(uuid);
    }

    // ==============================
    // 💾 LOAD & SAVE FILE .YML
    // ==============================

    public void saveData() {
        for (UUID uuid : storageData.keySet()) {
            for (Material material : storageData.get(uuid).keySet()) {
                int amount = storageData.get(uuid).get(material);
                dataConfig.set(uuid.toString() + "." + material.name(), amount);
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        for (String uuidStr : dataConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<Material, Integer> items = new HashMap<>();
            for (String matName : dataConfig.getConfigurationSection(uuidStr).getKeys(false)) {
                Material mat = Material.getMaterial(matName);
                int amount = dataConfig.getInt(uuidStr + "." + matName);
                if (mat != null) {
                    items.put(mat, amount);
                }
            }
            storageData.put(uuid, items);
        }
    }
}
