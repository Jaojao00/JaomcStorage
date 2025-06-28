package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SettingManager {

    private final Main plugin;
    private final FileConfiguration config;

    public SettingManager(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // =========================================================================
    // 📦 TÍNH NĂNG: Tự động lưu vào kho cho từng người chơi (autoStore)
    // =========================================================================

    // ✅ Kiểm tra xem người chơi đã bật chế độ tự lưu kho chưa
    public boolean isAutoStoreEnabled(Player player) {
        return config.getBoolean("autostore." + player.getUniqueId(), false);
    }

    // ✅ Gán trạng thái autoStore cho người chơi (bật/tắt)
    public void setAutoStore(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        config.set("autostore." + uuid, enabled);
        plugin.saveConfig();
    }

    // ✅ Đảo trạng thái autoStore của người chơi (toggle)
    public boolean toggleAutoStore(Player player) {
        boolean newState = !isAutoStoreEnabled(player);
        setAutoStore(player, newState);
        return newState;
    }

    // =========================================================================
    // ⚙️ HỖ TRỢ: Đọc dữ liệu từ file config.yml (non-static, instance-based)
    // =========================================================================

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public double getDouble(String path) {
        return config.getDouble(path, 0.0);
    }
}