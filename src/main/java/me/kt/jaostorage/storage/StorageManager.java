package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Quản lý kho lưu trữ vật phẩm.
 * Sử dụng in-memory cache + async SQLite write-through.
 */
public class StorageManager {

    private final Main plugin;
    private final DatabaseManager db;

    // === In-memory cache ===
    private final Map<UUID, Map<Material, Integer>> storageCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> slotsCache = new ConcurrentHashMap<>();
    private final Set<UUID> infinityCache = ConcurrentHashMap.newKeySet();
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();

    private int autoSaveTaskId = -1;

    public StorageManager(Main plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;

        // Migrate dữ liệu cũ nếu có
        File oldYaml = new File(plugin.getDataFolder(), "storage.yml");
        if (oldYaml.exists()) {
            db.migrateFromYaml(oldYaml);
        }

        loadAllData();
        startAutoSave();
    }

    // ==================== LOAD / SAVE ====================

    private void loadAllData() {
        Connection conn = db.getConnection();
        try {
            // Load storage
            try (PreparedStatement ps = conn.prepareStatement("SELECT uuid, material, amount FROM player_storage");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    Material mat = Material.getMaterial(rs.getString("material"));
                    int amount = rs.getInt("amount");
                    if (mat != null && amount > 0) {
                        storageCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(mat, amount);
                    }
                }
            }

            // Load settings (slots + infinity)
            try (PreparedStatement ps = conn.prepareStatement("SELECT uuid, upgraded_slots, infinity FROM player_settings");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int slots = rs.getInt("upgraded_slots");
                    boolean infinity = rs.getInt("infinity") == 1;
                    if (slots > 0) slotsCache.put(uuid, slots);
                    if (infinity) infinityCache.add(uuid);
                }
            }

            plugin.getLogger().info("✅ Đã tải dữ liệu cho " + storageCache.size() + " người chơi.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Không thể tải dữ liệu!", e);
        }
    }

    private void startAutoSave() {
        int interval = plugin.getConfig().getInt("AutoSaveInterval", 60) * 20; // ticks
        autoSaveTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveDirtyPlayers, interval, interval).getTaskId();
    }

    /**
     * Lưu dữ liệu của những player đã thay đổi (dirty).
     * Chạy async trên thread riêng.
     */
    public void saveDirtyPlayers() {
        if (dirtyPlayers.isEmpty()) return;

        Set<UUID> toSave = new HashSet<>(dirtyPlayers);
        dirtyPlayers.clear();

        Connection conn = db.getConnection();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psStorage = conn.prepareStatement(
                    "INSERT OR REPLACE INTO player_storage (uuid, material, amount) VALUES (?, ?, ?)");
                 PreparedStatement psDeleteZero = conn.prepareStatement(
                    "DELETE FROM player_storage WHERE uuid = ? AND material = ?");
                 PreparedStatement psSettings = conn.prepareStatement(
                    "INSERT OR REPLACE INTO player_settings (uuid, auto_store, upgraded_slots, infinity) " +
                    "VALUES (?, (SELECT COALESCE(auto_store, 0) FROM player_settings WHERE uuid = ?), ?, ?)")) {

                for (UUID uuid : toSave) {
                    String uuidStr = uuid.toString();

                    // Save storage items
                    Map<Material, Integer> items = storageCache.get(uuid);
                    if (items != null) {
                        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
                            if (entry.getValue() <= 0) {
                                psDeleteZero.setString(1, uuidStr);
                                psDeleteZero.setString(2, entry.getKey().name());
                                psDeleteZero.addBatch();
                            } else {
                                psStorage.setString(1, uuidStr);
                                psStorage.setString(2, entry.getKey().name());
                                psStorage.setInt(3, entry.getValue());
                                psStorage.addBatch();
                            }
                        }
                    }

                    // Save settings
                    psSettings.setString(1, uuidStr);
                    psSettings.setString(2, uuidStr);
                    psSettings.setInt(3, getSlots(uuid));
                    psSettings.setInt(4, isInfinity(uuid) ? 1 : 0);
                    psSettings.addBatch();
                }

                psStorage.executeBatch();
                psDeleteZero.executeBatch();
                psSettings.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            plugin.getLogger().log(Level.SEVERE, "❌ Lỗi khi lưu dữ liệu!", e);
            // Re-add to dirty set để thử lại lần sau
            dirtyPlayers.addAll(toSave);
        }
    }

    /**
     * Lưu toàn bộ dữ liệu (gọi khi shutdown).
     */
    public void saveAll() {
        // Đánh dấu tất cả là dirty rồi save
        dirtyPlayers.addAll(storageCache.keySet());
        saveDirtyPlayers();
    }

    public void shutdown() {
        if (autoSaveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autoSaveTaskId);
        }
        saveAll();
    }

    // ==================== STORAGE OPERATIONS ====================

    public int getAmount(UUID uuid, Material material) {
        Map<Material, Integer> items = storageCache.get(uuid);
        return items != null ? items.getOrDefault(material, 0) : 0;
    }

    public void setAmount(UUID uuid, Material material, int amount) {
        storageCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(material, Math.max(0, amount));
        dirtyPlayers.add(uuid);
    }

    /**
     * Thêm vật phẩm vào kho. Kiểm tra MaxSpace nếu không phải infinity.
     * @return số lượng thực sự được thêm (có thể < amount nếu đầy)
     */
    public int addItem(UUID uuid, Material material, int amount) {
        if (amount <= 0) return 0;

        int maxSpace = plugin.getConfig().getInt("MaxSpace", 100000);
        int currentTotal = getTotalItems(uuid);
        int current = getAmount(uuid, material);

        int canAdd;
        if (isInfinity(uuid)) {
            canAdd = amount;
        } else {
            int remaining = maxSpace - currentTotal;
            canAdd = Math.min(amount, remaining);
        }

        if (canAdd <= 0) return 0;

        setAmount(uuid, material, current + canAdd);
        return canAdd;
    }

    /**
     * Xóa vật phẩm khỏi kho.
     * @return true nếu đủ số lượng để xóa
     */
    public boolean removeItem(UUID uuid, Material material, int amount) {
        int current = getAmount(uuid, material);
        if (current < amount) return false;
        setAmount(uuid, material, current - amount);
        return true;
    }

    public Map<Material, Integer> getStorage(UUID uuid) {
        return storageCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
    }

    /**
     * Tổng số vật phẩm trong kho (dùng để check MaxSpace).
     */
    public int getTotalItems(UUID uuid) {
        Map<Material, Integer> items = storageCache.get(uuid);
        if (items == null) return 0;
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Kiểm tra kho có đầy không.
     */
    public boolean isFull(UUID uuid) {
        if (isInfinity(uuid)) return false;
        int maxSpace = plugin.getConfig().getInt("MaxSpace", 100000);
        return getTotalItems(uuid) >= maxSpace;
    }

    /**
     * Dung lượng kho còn lại.
     */
    public int getRemainingSpace(UUID uuid) {
        if (isInfinity(uuid)) return Integer.MAX_VALUE;
        int maxSpace = plugin.getConfig().getInt("MaxSpace", 100000);
        return Math.max(0, maxSpace - getTotalItems(uuid));
    }

    public boolean isStorable(Material material) {
        return plugin.getOreConfig().getOres().contains(material);
    }

    // ==================== SLOT MANAGEMENT ====================

    public int getSlots(UUID uuid) {
        return slotsCache.getOrDefault(uuid, 0);
    }

    public void upgradeStorage(UUID uuid, int slots) {
        slotsCache.put(uuid, getSlots(uuid) + slots);
        dirtyPlayers.add(uuid);
    }

    // ==================== INFINITY MODE ====================

    public boolean isInfinity(UUID uuid) {
        return infinityCache.contains(uuid);
    }

    public void setInfinity(UUID uuid, boolean enabled) {
        if (enabled) infinityCache.add(uuid);
        else infinityCache.remove(uuid);
        dirtyPlayers.add(uuid);
    }

    public boolean toggleInfinity(UUID uuid) {
        boolean newState = !isInfinity(uuid);
        setInfinity(uuid, newState);
        return newState;
    }
}