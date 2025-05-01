package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SettingManager {

    // 🔁 Biến static để dùng cho config.yml toàn cục
    private static FileConfiguration staticConfig;

    // 🔧 Biến nội bộ plugin
    private final Main plugin;
    private final FileConfiguration config;

    // 🧱 Khởi tạo SettingManager với file config chính
    public SettingManager(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        staticConfig = config; // gán để các phương thức static có thể dùng
    }

    // =================================================================
    // 📦 TÍNH NĂNG: Tự động lưu vào kho cho từng người chơi (autoStore)
    // =================================================================

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

    // =================================================================
    // ⚙️ HỖ TRỢ: Đọc dữ liệu từ file config.yml dạng static
    // =================================================================

    // ✅ Đọc giá trị boolean trong config.yml (true/false)
    public static boolean getBoolean(String path) {
        return staticConfig.getBoolean(path, false);
    }

    // ✅ Đọc chuỗi (String) trong config.yml
    public static String getString(String path) {
        return staticConfig.getString(path, "");
    }

    // ✅ Đọc danh sách chuỗi (List<String>) trong config.yml
    public static List<String> getStringList(String path) {
        return staticConfig.getStringList(path);
    }

    // ✅ Đọc số nguyên (int) trong config.yml
    public static int getInt(String path) {
        return staticConfig.getInt(path, 0);
    }
}
