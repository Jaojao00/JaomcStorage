package me.kt.jaostorage.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Custom InventoryHolder để nhận dạng GUI kho lưu trữ.
 * Giúp xác định chính xác GUI nào đang mở mà không cần kiểm tra title.
 */
public class StorageHolder implements InventoryHolder {

    public enum GUIType {
        MAIN_STORAGE,    // GUI kho chính (54 slots)
        ITEM_MENU,       // Menu thao tác item (9 slots)
        SELL_CONFIRM     // Xác nhận bán (9 slots)
    }

    private final UUID viewerUUID;    // Người đang xem
    private final UUID targetUUID;    // Kho của ai
    private final GUIType guiType;
    private final String extraData;   // Dữ liệu bổ sung (tên material, etc.)

    public StorageHolder(UUID viewerUUID, UUID targetUUID, GUIType guiType) {
        this(viewerUUID, targetUUID, guiType, null);
    }

    public StorageHolder(UUID viewerUUID, UUID targetUUID, GUIType guiType, String extraData) {
        this.viewerUUID = viewerUUID;
        this.targetUUID = targetUUID;
        this.guiType = guiType;
        this.extraData = extraData;
    }

    public UUID getViewerUUID() { return viewerUUID; }
    public UUID getTargetUUID() { return targetUUID; }
    public GUIType getGuiType() { return guiType; }
    public String getExtraData() { return extraData; }

    @Override
    public Inventory getInventory() {
        return null; // Không cần implement - Bukkit tự quản lý
    }
}
