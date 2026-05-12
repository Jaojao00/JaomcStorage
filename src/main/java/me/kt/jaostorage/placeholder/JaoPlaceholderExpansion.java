package me.kt.jaostorage.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

/**
 * Tích hợp PlaceholderAPI cho JaomcStorage.
 *
 * Placeholders:
 *   %jaostorage_slot%           → Số slot nâng cấp
 *   %jaostorage_infinity%       → true/false kho vô hạn
 *   %jaostorage_total%          → Tổng items trong kho
 *   %jaostorage_maxspace%       → Dung lượng tối đa
 *   %jaostorage_remaining%      → Dung lượng còn lại
 *   %jaostorage_autostore%      → Trạng thái autostore
 *   %jaostorage_coop_count%     → Số coop members
 *   %jaostorage_amount_DIAMOND% → Số lượng Diamond
 */
public class JaoPlaceholderExpansion extends PlaceholderExpansion {

    private final Main plugin;
    private final StorageManager storageManager;

    public JaoPlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    @Override
    public String getIdentifier() {
        return "jaostorage";
    }

    @Override
    public String getAuthor() {
        return "TaiNguyen";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        var uuid = player.getUniqueId();

        return switch (params.toLowerCase()) {
            case "slot" -> String.valueOf(storageManager.getSlots(uuid));
            case "infinity" -> String.valueOf(storageManager.isInfinity(uuid));
            case "total" -> MessageUtil.formatNumber(storageManager.getTotalItems(uuid));
            case "maxspace" -> MessageUtil.formatNumber(plugin.getConfig().getInt("MaxSpace", 100000));
            case "remaining" -> MessageUtil.formatNumber(storageManager.getRemainingSpace(uuid));
            case "autostore" -> String.valueOf(plugin.getSettingManager().isAutoStoreEnabled(uuid));
            case "coop_count" -> String.valueOf(plugin.getCoopManager().getMemberCount(uuid));
            default -> {
                // %jaostorage_amount_MATERIAL%
                if (params.startsWith("amount_")) {
                    String item = params.substring("amount_".length()).toUpperCase();
                    Material material = Material.matchMaterial(item);
                    if (material != null) {
                        yield MessageUtil.formatNumber(storageManager.getAmount(uuid, material));
                    }
                }
                yield null;
            }
        };
    }
}