package me.kt.jaostorage.gui;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.storage.SettingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StorageGUI {

    private final StorageManager storageManager;
    private final SettingManager settingManager;
    private final FileConfiguration config;

    public StorageGUI(Main plugin) {
        this.storageManager = plugin.getStorageManager();
        this.settingManager = new SettingManager(plugin);
        this.config = plugin.getConfig(); // ✅ Lấy config để đọc tên hiển thị
    }

    // ✅ Mở kho giao diện chính
    public void openStorage(Player player) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, "📦 Khoáng sản của bạn");

        Map<Material, Integer> storage = storageManager.getStorage(player);

        List<Integer> itemSlots = Arrays.asList(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        );

        List<Material> ores = Arrays.asList(
                Material.COAL, Material.IRON_INGOT, Material.COPPER_INGOT,
                Material.GOLD_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI,
                Material.EMERALD, Material.DIAMOND, Material.NETHERITE_SCRAP,
                Material.EMERALD_BLOCK, Material.DIAMOND_BLOCK, Material.IRON_BLOCK,
                Material.COAL_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK,
                Material.LAPIS_BLOCK, Material.STONE, Material.COAL_BLOCK,
                Material.RAW_GOLD, Material.RAW_IRON, Material.COBBLESTONE
        );

        // Viền nền
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = blackGlass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            blackGlass.setItemMeta(meta);
        }

        for (int i = 0; i < 54; i++) {
            if (!itemSlots.contains(i)) {
                gui.setItem(i, blackGlass);
            }
        }

// ✅ Gán ores vào slot và đọc tên + lore từ config
        for (int i = 0; i < itemSlots.size(); i++) {
            if (i >= ores.size()) break;
            int slot = itemSlots.get(i);
            Material material = ores.get(i);
            int amount = storage.getOrDefault(material, 0);

            ItemStack item = new ItemStack(material);
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                // 📛 Tên hiển thị
                String displayName = config.getString("FormatName." + material.name(), material.name());
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

                // 📝 Lore: dùng FormatLore nếu có
                List<String> lore = new ArrayList<>();
                List<String> rawLore = config.getStringList("FormatLore." + material.name());
                if (!rawLore.isEmpty()) {
                    // Giá bán từ Prices
                    double price = config.getDouble("Prices." + material.name(), 0.0);

                    for (String line : rawLore) {
                        String parsed = line
                                .replace("{amount}", String.valueOf(amount))
                                .replace("{price}", String.valueOf(price));
                        lore.add(ChatColor.translateAlternateColorCodes('&', parsed));
                    }
                } else {
                    // Nếu không có FormatLore -> dùng mặc định
                    lore.add("§7Số lượng: " + amount);
                    lore.add("§aNhấn để thao tác");
                }

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
            }

            gui.setItem(slot, item);
        }


        // ✅ Tạo nút BẬT/TẮT AutoStore ở slot 45
        boolean autoStore = settingManager.isAutoStoreEnabled(player);
        ItemStack toggleItem = new ItemStack(autoStore ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleItem.getItemMeta();
        if (toggleMeta != null) {
            toggleMeta.setDisplayName(ChatColor.YELLOW + "Tự động lưu vào kho: " + (autoStore ? "§aBẬT" : "§cTẮT"));
            toggleMeta.setLore(Collections.singletonList("§7Click để " + (autoStore ? "§ctắt" : "§abật")));
            toggleItem.setItemMeta(toggleMeta);
        }
        gui.setItem(45, toggleItem);

        player.openInventory(gui);
    }

    // 🟡 Trang trí sau nếu cần
    private ItemStack createGlassPane(String name) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            glass.setItemMeta(meta);
        }
        return glass;
    }

    // ✅ Mở menu thao tác: Rút, Cất, Bán
    public void openItemMenu(Player player, Material material) {
        Inventory menu = Bukkit.createInventory(null, 9, "⚙ " + material.name());

        menu.setItem(2, createButton(Material.CHEST, "§aCất vào kho", "§7Nhập số lượng vào chat hoặc 'all'"));
        menu.setItem(4, createButton(Material.HOPPER, "§eRút khỏi kho", "§7Nhập số lượng vào chat hoặc 'all'"));
        menu.setItem(6, createButton(Material.EMERALD, "§6Bán khoáng sản", "§7Click giữa để bán toàn bộ"));

        player.openInventory(menu);
    }

    private ItemStack createButton(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ✅ Cho phép gọi mở kho từ nơi khác (ví dụ: /kho)
    public static void open(Player player) {
        Main plugin = Main.getInstance();
        new StorageGUI(plugin).openStorage(player);
    }
}
