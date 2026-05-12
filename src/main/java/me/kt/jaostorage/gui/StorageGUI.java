package me.kt.jaostorage.gui;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.CoopManager;
import me.kt.jaostorage.storage.SettingManager;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class StorageGUI {

    private final Main plugin;
    private final StorageManager storageManager;
    private final SettingManager settingManager;
    private final FileConfiguration config;

    // ═══════ DESIGN CONSTANTS ═══════
    // Gradient border slots (outer ring)
    private static final int[] CORNER_SLOTS = {0, 8, 45, 53};
    private static final int[] TOP_BOTTOM_EDGE = {1,2,3,4,5,6,7, 46,47,48,49,50,51,52};
    private static final int[] SIDE_EDGE = {9, 17, 18, 26, 27, 35, 36, 44};

    // Item display slots (3 rows x 7 cols centered)
    private static final List<Integer> ITEM_SLOTS = Arrays.asList(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    );

    // Separator row
    private static final int[] SEPARATOR_SLOTS = {37, 38, 39, 40, 41, 42, 43};

    // Ores to display
    private static final List<Material> DISPLAY_ORES = Arrays.asList(
        Material.COAL, Material.RAW_IRON, Material.RAW_COPPER,
        Material.RAW_GOLD, Material.REDSTONE, Material.LAPIS_LAZULI,
        Material.EMERALD, Material.DIAMOND, Material.QUARTZ,
        Material.COBBLESTONE, Material.STONE, Material.GOLD_NUGGET,
        Material.IRON_INGOT, Material.COPPER_INGOT, Material.GOLD_INGOT,
        Material.NETHERITE_SCRAP, Material.EMERALD_BLOCK, Material.DIAMOND_BLOCK,
        Material.IRON_BLOCK, Material.COAL_BLOCK, Material.GOLD_BLOCK
    );

    // Rarity colors for items
    private static final Map<Material, String> RARITY = new LinkedHashMap<>();
    static {
        RARITY.put(Material.NETHERITE_SCRAP, "§4§l✦ LEGENDARY");
        RARITY.put(Material.DIAMOND, "§b§l✦ RARE");
        RARITY.put(Material.EMERALD, "§a§l✦ RARE");
        RARITY.put(Material.DIAMOND_BLOCK, "§b§l✦ EPIC");
        RARITY.put(Material.EMERALD_BLOCK, "§a§l✦ EPIC");
        RARITY.put(Material.GOLD_INGOT, "§e§l✦ UNCOMMON");
        RARITY.put(Material.IRON_INGOT, "§f§l✦ UNCOMMON");
        RARITY.put(Material.RAW_GOLD, "§e✦ UNCOMMON");
        RARITY.put(Material.LAPIS_LAZULI, "§9✦ UNCOMMON");
    }

    public StorageGUI(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.settingManager = plugin.getSettingManager();
        this.config = plugin.getConfig();
    }

    // ═══════════════════════════════════════════════
    //  📦 MAIN STORAGE GUI
    // ═══════════════════════════════════════════════

    public void openStorageGUI(Player viewer, UUID targetUUID) {
        StorageHolder holder = new StorageHolder(
            viewer.getUniqueId(), targetUUID, StorageHolder.GUIType.MAIN_STORAGE
        );

        boolean isOwn = viewer.getUniqueId().equals(targetUUID);
        String title = isOwn
            ? "§8┃ §6§l⛏ KHO KHOÁNG SẢN §8┃"
            : "§8┃ §6§l⛏ Kho của §e" + getPlayerName(targetUUID) + " §8┃";

        Inventory gui = Bukkit.createInventory(holder, 54, title);
        Map<Material, Integer> storage = storageManager.getStorage(targetUUID);

        // ─── Gradient Border ───
        fillBorder(gui);

        // ─── Separator Row (row 5) ───
        ItemStack sepGlass = createGlass(Material.CYAN_STAINED_GLASS_PANE, "§8§m─────────────────");
        for (int s : SEPARATOR_SLOTS) gui.setItem(s, sepGlass);

        // ─── Item Display ───
        for (int i = 0; i < ITEM_SLOTS.size() && i < DISPLAY_ORES.size(); i++) {
            int slot = ITEM_SLOTS.get(i);
            Material material = DISPLAY_ORES.get(i);
            int amount = storage.getOrDefault(material, 0);
            gui.setItem(slot, createOreItem(material, amount));
        }

        // ─── Bottom Bar: Functional Buttons ───
        // Slot 45: AutoStore Toggle
        boolean autoStore = settingManager.isAutoStoreEnabled(viewer);
        gui.setItem(45, createAutoStoreButton(autoStore));

        // Slot 47: Capacity Info
        gui.setItem(47, createCapacityItem(targetUUID));

        // Slot 49: Sell All
        gui.setItem(49, createSellAllButton(targetUUID));

        // Slot 51: Coop Info
        gui.setItem(51, createCoopButton(targetUUID));

        // Slot 53: Close
        gui.setItem(53, createButton(Material.BARRIER,
            "§c§l✖ §cĐóng",
            Arrays.asList("§8§m                    ", "§7Click để đóng kho", "§8§m                    ")
        ));

        viewer.openInventory(gui);
    }

    // ═══════════════════════════════════════════════
    //  ⚙ ITEM ACTION MENU
    // ═══════════════════════════════════════════════

    public void openItemMenu(Player viewer, UUID targetUUID, Material material) {
        StorageHolder holder = new StorageHolder(
            viewer.getUniqueId(), targetUUID,
            StorageHolder.GUIType.ITEM_MENU, material.name()
        );

        int amount = storageManager.getAmount(targetUUID, material);
        String displayName = getDisplayName(material);
        double price = config.getDouble("Prices." + material.name(), 0.0);
        String currency = config.getString("Economy.Currency", "$");
        String rarity = RARITY.getOrDefault(material, "§7✦ COMMON");

        Inventory menu = Bukkit.createInventory(holder, 27,
            "§8┃ §e§l⚙ " + ChatColor.stripColor(MessageUtil.color(displayName)) + " §8┃");

        // Fill background
        ItemStack bg = createGlass(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) menu.setItem(i, bg);

        // Decorative top/bottom
        ItemStack accent = createGlass(Material.YELLOW_STAINED_GLASS_PANE, " ");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8, 18,19,20,21,22,23,24,25,26}) {
            menu.setItem(i, accent);
        }

        // Center: Item Info (slot 13)
        ItemStack infoItem = new ItemStack(material);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtil.color(getDisplayName(material)));
            infoMeta.setLore(Arrays.asList(
                "§8§m                         ",
                rarity,
                "",
                "§8  ▪ §7Trong kho: §f§l" + MessageUtil.formatNumber(amount),
                "§8  ▪ §7Giá bán:   §e" + MessageUtil.formatNumber(price) + currency + "§7/cái",
                "§8  ▪ §7Tổng giá:  §6" + MessageUtil.formatNumber(price * amount) + currency,
                "",
                "§8§m                         "
            ));
            infoItem.setItemMeta(infoMeta);
        }
        menu.setItem(13, infoItem);

        // Store button (slot 10)
        menu.setItem(10, createButton(Material.CHEST,
            "§a§l📥 CẤT VÀO KHO",
            Arrays.asList(
                "§8§m                    ",
                "§7Chuyển vật phẩm từ",
                "§7túi đồ vào kho lưu trữ",
                "",
                "§a  ▸ §fClick §7rồi nhập số",
                "§a  ▸ §fGõ 'all' §7để cất hết",
                "§a  ▸ §fGõ 'cancel' §7để hủy",
                "§8§m                    "
            )
        ));

        // Withdraw button (slot 11)
        menu.setItem(11, createButton(Material.HOPPER,
            "§e§l📤 RÚT KHỎI KHO",
            Arrays.asList(
                "§8§m                    ",
                "§7Lấy vật phẩm từ kho",
                "§7ra túi đồ của bạn",
                "",
                "§e  ▸ §fClick §7rồi nhập số",
                "§e  ▸ §fGõ 'all' §7để rút hết",
                "§e  ▸ §fGõ 'cancel' §7để hủy",
                "§8§m                    "
            )
        ));

        // Sell button (slot 15)
        menu.setItem(15, createButton(Material.GOLD_NUGGET,
            "§6§l💰 BÁN",
            Arrays.asList(
                "§8§m                    ",
                "§7Bán lấy tiền (Vault)",
                "",
                "§8  ▪ §7Giá: §e" + MessageUtil.formatNumber(price) + currency + "§7/cái",
                "§8  ▪ §7Có: §f" + MessageUtil.formatNumber(amount) + " §7cái",
                "§8  ▪ §7Tổng: §6§l" + MessageUtil.formatNumber(price * amount) + currency,
                "",
                "§6  ▸ §fClick trái: §7Nhập số",
                "§c  ▸ §fClick giữa: §7Bán hết",
                "§8§m                    "
            )
        ));

        // Sell ALL quick (slot 16)
        menu.setItem(16, createButton(Material.EMERALD,
            "§6§l⚡ BÁN TẤT CẢ",
            Arrays.asList(
                "§8§m                    ",
                "§7Bán toàn bộ §f" + MessageUtil.formatNumber(amount),
                "§7" + ChatColor.stripColor(MessageUtil.color(getDisplayName(material))),
                "",
                "§7Nhận: §6§l" + MessageUtil.formatNumber(price * amount) + currency,
                "",
                "§c⚠ Click để bán ngay!",
                "§8§m                    "
            )
        ));

        // Back button (slot 22)
        menu.setItem(22, createButton(Material.ARROW,
            "§c§l↩ §cQuay Lại",
            Collections.singletonList("§7Về kho chính")
        ));

        viewer.openInventory(menu);
    }

    // ═══════════════════════════════════════════════
    //  ⚠ SELL CONFIRM GUI
    // ═══════════════════════════════════════════════

    public void openSellConfirm(Player viewer, UUID targetUUID) {
        StorageHolder holder = new StorageHolder(
            viewer.getUniqueId(), targetUUID, StorageHolder.GUIType.SELL_CONFIRM
        );

        Inventory confirm = Bukkit.createInventory(holder, 27,
            "§8┃ §c§l⚠ XÁC NHẬN BÁN TẤT CẢ §8┃");

        // Background
        ItemStack bg = createGlass(Material.RED_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) confirm.setItem(i, bg);

        // Calculate total
        Map<Material, Integer> storage = storageManager.getStorage(targetUUID);
        double totalValue = 0;
        int totalItems = 0;
        int itemTypes = 0;
        List<String> itemList = new ArrayList<>();

        for (Map.Entry<Material, Integer> entry : storage.entrySet()) {
            if (entry.getValue() <= 0) continue;
            double price = config.getDouble("Prices." + entry.getKey().name(), 0.0);
            if (price <= 0) continue;
            double value = price * entry.getValue();
            totalValue += value;
            totalItems += entry.getValue();
            itemTypes++;
            if (itemList.size() < 8) {
                String name = getDisplayName(entry.getKey());
                itemList.add("§8  ▪ §f" + MessageUtil.formatNumber(entry.getValue()) + "x §7"
                    + ChatColor.stripColor(MessageUtil.color(name))
                    + " §8→ §e" + MessageUtil.formatNumber(value) + config.getString("Economy.Currency", "$"));
            }
        }
        if (itemTypes > 8) {
            itemList.add("§8  ... và " + (itemTypes - 8) + " loại khác");
        }

        String currency = config.getString("Economy.Currency", "$");

        // Info panel (slot 13)
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§8§m                              ");
        infoLore.add("§c⚠ §7Hành động này §c§lKHÔNG THỂ HOÀN TÁC§7!");
        infoLore.add("");
        infoLore.add("§7Sẽ bán: §f§l" + MessageUtil.formatNumber(totalItems) + " §7vật phẩm");
        infoLore.add("§7Loại:   §f" + itemTypes + " §7loại");
        infoLore.add("§7Nhận:   §6§l" + MessageUtil.formatNumber(totalValue) + currency);
        infoLore.add("");
        infoLore.addAll(itemList);
        infoLore.add("§8§m                              ");

        confirm.setItem(13, createButton(Material.PAPER, "§e§l📋 CHI TIẾT GIAO DỊCH", infoLore));

        // Confirm (slot 11)
        confirm.setItem(11, createButton(Material.LIME_WOOL,
            "§a§l✓ XÁC NHẬN BÁN",
            Arrays.asList(
                "§8§m                    ",
                "§7Bán §f" + MessageUtil.formatNumber(totalItems) + " §7vật phẩm",
                "§7Nhận §6§l" + MessageUtil.formatNumber(totalValue) + currency,
                "",
                "§a§l  ▸ CLICK ĐỂ XÁC NHẬN",
                "§8§m                    "
            )
        ));

        // Cancel (slot 15)
        confirm.setItem(15, createButton(Material.RED_WOOL,
            "§c§l✖ HỦY BỎ",
            Arrays.asList(
                "§8§m                    ",
                "§7Quay lại kho",
                "§7Không bán gì cả",
                "",
                "§c  ▸ Click để hủy",
                "§8§m                    "
            )
        ));

        viewer.openInventory(confirm);
    }

    // ═══════════════════════════════════════════════
    //  🎨 GUI COMPONENT BUILDERS
    // ═══════════════════════════════════════════════

    private void fillBorder(Inventory gui) {
        // Corners: Dark cyan
        ItemStack corner = createGlass(Material.CYAN_STAINED_GLASS_PANE, "§3§m  ");
        for (int s : CORNER_SLOTS) gui.setItem(s, corner);

        // Top/Bottom edges: Light blue gradient
        ItemStack edge = createGlass(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        for (int s : TOP_BOTTOM_EDGE) gui.setItem(s, edge);

        // Side edges: Blue
        ItemStack side = createGlass(Material.BLUE_STAINED_GLASS_PANE, " ");
        for (int s : SIDE_EDGE) gui.setItem(s, side);
    }

    private ItemStack createOreItem(Material material, int amount) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Display name with amount badge
        String displayName = getDisplayName(material);
        String rarity = RARITY.getOrDefault(material, null);
        String badge = amount > 0 ? " §8[§f" + MessageUtil.formatNumber(amount) + "§8]" : " §8[§70§8]";
        meta.setDisplayName(MessageUtil.color(displayName) + badge);

        // Lore
        List<String> lore = new ArrayList<>();
        lore.add("§8§m                         ");

        // Rarity tag
        if (rarity != null) {
            lore.add(rarity);
            lore.add("");
        }

        // Custom lore from config
        List<String> rawLore = config.getStringList("FormatLore." + material.name());
        double price = config.getDouble("Prices." + material.name(), 0.0);

        if (!rawLore.isEmpty()) {
            for (String line : rawLore) {
                String parsed = line
                    .replace("{amount}", MessageUtil.formatNumber(amount))
                    .replace("{price}", MessageUtil.formatNumber(price));
                lore.add(MessageUtil.color(parsed));
            }
        } else {
            lore.add("§8  ▪ §7Số lượng: §f" + MessageUtil.formatNumber(amount));
            if (price > 0) {
                String currency = config.getString("Economy.Currency", "$");
                lore.add("§8  ▪ §7Giá bán:  §e" + MessageUtil.formatNumber(price) + currency);
                lore.add("§8  ▪ §7Tổng:     §6" + MessageUtil.formatNumber(price * amount) + currency);
            }
        }

        lore.add("");
        lore.add("§8§m                         ");
        lore.add(amount > 0 ? "§a  ▸ Click để thao tác" : "§8  ▸ Chưa có vật phẩm");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createAutoStoreButton(boolean enabled) {
        Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "§a§lBẬT" : "§c§lTẮT";
        String icon = enabled ? "§a⚡" : "§8⚡";

        return createButton(mat,
            icon + " §eTự động lưu: " + status,
            Arrays.asList(
                "§8§m                    ",
                enabled
                    ? "§a  ✓ §7Quặng đào được sẽ"
                    : "§c  ✗ §7Quặng đào được sẽ",
                enabled
                    ? "§a    §7tự động vào kho"
                    : "§c    §7rơi xuống đất",
                "",
                "§e  ▸ Click để " + (enabled ? "§ctắt" : "§abật"),
                "§8§m                    "
            )
        );
    }

    private ItemStack createCapacityItem(UUID targetUUID) {
        int totalItems = storageManager.getTotalItems(targetUUID);
        int maxSpace = config.getInt("MaxSpace", 100000);
        boolean infinity = storageManager.isInfinity(targetUUID);

        // Build visual progress bar
        int percent = infinity ? 0 : (maxSpace > 0 ? Math.min(100, totalItems * 100 / maxSpace) : 100);
        String bar = buildProgressBar(percent, 20);

        String capacityStr = infinity
            ? "§a§l∞ Vô hạn"
            : "§f" + MessageUtil.formatNumber(totalItems) + " §8/ §f" + MessageUtil.formatNumber(maxSpace);

        String percentColor = percent > 90 ? "§c§l" : percent > 70 ? "§e" : "§a";

        return createButton(Material.ENDER_CHEST,
            "§6§l📊 Dung Lượng Kho",
            Arrays.asList(
                "§8§m                         ",
                "",
                "§7  " + bar + " " + percentColor + percent + "%",
                "",
                "§8  ▪ §7Đã dùng:    " + capacityStr,
                "§8  ▪ §7Còn trống:  §f" + (infinity ? "∞"
                    : MessageUtil.formatNumber(storageManager.getRemainingSpace(targetUUID))),
                "§8  ▪ §7Slot nâng cấp: §b" + storageManager.getSlots(targetUUID),
                "",
                infinity ? "§a§l  ★ §aChế độ VIP Vô hạn" : "§7  Nâng cấp để tăng dung lượng",
                "§8§m                         "
            )
        );
    }

    private ItemStack createSellAllButton(UUID targetUUID) {
        Map<Material, Integer> storage = storageManager.getStorage(targetUUID);
        double totalValue = 0;
        int totalItems = 0;
        for (Map.Entry<Material, Integer> entry : storage.entrySet()) {
            if (entry.getValue() <= 0) continue;
            double price = config.getDouble("Prices." + entry.getKey().name(), 0.0);
            totalValue += price * entry.getValue();
            totalItems += entry.getValue();
        }

        String currency = config.getString("Economy.Currency", "$");

        return createButton(Material.GOLD_INGOT,
            "§6§l💰 Bán Tất Cả",
            Arrays.asList(
                "§8§m                    ",
                "§7Bán toàn bộ khoáng sản",
                "§7trong kho để lấy tiền",
                "",
                "§8  ▪ §7Vật phẩm: §f" + MessageUtil.formatNumber(totalItems),
                "§8  ▪ §7Tổng giá: §6§l" + MessageUtil.formatNumber(totalValue) + currency,
                "",
                "§c  ⚠ §7Sẽ yêu cầu xác nhận",
                "§8§m                    "
            )
        );
    }

    private ItemStack createCoopButton(UUID targetUUID) {
        int coopCount = plugin.getCoopManager().getMemberCount(targetUUID);
        Set<UUID> members = plugin.getCoopManager().getMembers(targetUUID);

        List<String> lore = new ArrayList<>();
        lore.add("§8§m                    ");

        if (members.isEmpty()) {
            lore.add("§7Chưa chia sẻ kho với ai");
        } else {
            lore.add("§7Đang chia sẻ với:");
            for (UUID memberUUID : members) {
                var member = Bukkit.getOfflinePlayer(memberUUID);
                String status = member.isOnline() ? "§a●" : "§c●";
                lore.add("§8  " + status + " §f" + (member.getName() != null ? member.getName() : "???"));
            }
        }

        lore.add("");
        lore.add("§b  ▸ §7/kho coop add <tên>");
        lore.add("§b  ▸ §7/kho coop remove <tên>");
        lore.add("§b  ▸ §7/kho coop list");
        lore.add("§8§m                    ");

        return createButton(Material.PLAYER_HEAD,
            "§b§l👥 Coop §8[§f" + coopCount + "§8]",
            lore
        );
    }

    // ═══════════════════════════════════════════════
    //  🛠 UTILITY METHODS
    // ═══════════════════════════════════════════════

    private String buildProgressBar(int percent, int length) {
        int filled = (int) Math.round(length * percent / 100.0);
        int empty = length - filled;

        String filledColor = percent > 90 ? "§c" : percent > 70 ? "§e" : "§a";

        StringBuilder bar = new StringBuilder("§8[");
        bar.append(filledColor);
        for (int i = 0; i < filled; i++) bar.append("▮");
        bar.append("§7");
        for (int i = 0; i < empty; i++) bar.append("▯");
        bar.append("§8]");
        return bar.toString();
    }

    private String getDisplayName(Material material) {
        return config.getString("FormatName." + material.name(), material.name());
    }

    private ItemStack createGlass(Material material, String name) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            glass.setItemMeta(meta);
        }
        return glass;
    }

    private ItemStack createButton(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getPlayerName(UUID uuid) {
        var player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() != null ? player.getName() : uuid.toString().substring(0, 8);
    }
}
