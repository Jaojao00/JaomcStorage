package me.kt.jaostorage.gui;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.*;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StorageGUIListener implements Listener {

    private final Main plugin;
    private final StorageManager storageManager;
    private final EconomyManager economyManager;
    private final LogManager logManager;
    private final StorageGUI storageGUI;

    private final Map<UUID, ActionContext> chatWaiting = new ConcurrentHashMap<>();

    public StorageGUIListener(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.economyManager = plugin.getEconomyManager();
        this.logManager = plugin.getLogManager();
        this.storageGUI = plugin.getStorageGUI();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof StorageHolder holder)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Material clickedType = event.getCurrentItem().getType();
        if (clickedType.name().contains("GLASS_PANE")) return;
        if (clickedType == Material.AIR) return;

        switch (holder.getGuiType()) {
            case MAIN_STORAGE -> handleMainStorage(player, holder, event);
            case ITEM_MENU -> handleItemMenu(player, holder, event);
            case SELL_CONFIRM -> handleSellConfirm(player, holder, event);
        }
    }

    // ══════════ MAIN STORAGE ══════════

    private void handleMainStorage(Player player, StorageHolder holder, InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        Material clickedType = event.getCurrentItem().getType();

        // AutoStore toggle (slot 45)
        if (rawSlot == 45) {
            boolean newState = plugin.getSettingManager().toggleAutoStore(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, newState ? 1.5f : 0.8f);
            MessageUtil.send(player, "⚡ Tự động lưu: " + (newState ? "&a&lBẬT" : "&c&lTẮT"));
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                storageGUI.openStorageGUI(player, holder.getTargetUUID()), 1L);
            return;
        }

        // Capacity info (slot 47) - no action
        if (rawSlot == 47) return;

        // Sell All (slot 49)
        if (rawSlot == 49) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
            storageGUI.openSellConfirm(player, holder.getTargetUUID());
            return;
        }

        // Coop info (slot 51)
        if (rawSlot == 51) {
            player.closeInventory();
            MessageUtil.send(player, "&b&m                                    ");
            MessageUtil.send(player, "&b&l  👥 Quản lý Coop");
            MessageUtil.send(player, "");
            MessageUtil.send(player, "&e  /kho coop add <tên> &8- &7Thêm");
            MessageUtil.send(player, "&e  /kho coop remove <tên> &8- &7Xóa");
            MessageUtil.send(player, "&e  /kho coop list &8- &7Danh sách");
            MessageUtil.send(player, "&b&m                                    ");
            return;
        }

        // Close (slot 53)
        if (rawSlot == 53 && clickedType == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Click ore item → open item menu
        if (isOreSlot(rawSlot) && !isUiMaterial(clickedType)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            storageGUI.openItemMenu(player, holder.getTargetUUID(), clickedType);
        }
    }

    // ══════════ ITEM MENU ══════════

    private void handleItemMenu(Player player, StorageHolder holder, InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        String matName = holder.getExtraData();
        Material material = Material.matchMaterial(matName);

        if (material == null) {
            MessageUtil.send(player, "&cLỗi: Không xác định được vật phẩm!");
            return;
        }

        // Store (slot 10)
        if (rawSlot == 10) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            MessageUtil.send(player, "&a&m                                    ");
            MessageUtil.send(player, "&a  📥 &fNhập số lượng cần &aCẤT VÀO KHO");
            MessageUtil.send(player, "&7  Gõ số, 'all' để cất hết, 'cancel' để hủy");
            MessageUtil.send(player, "&a&m                                    ");
            chatWaiting.put(player.getUniqueId(),
                new ActionContext(material, ActionType.STORE, holder.getTargetUUID()));
            return;
        }

        // Withdraw (slot 11)
        if (rawSlot == 11) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            MessageUtil.send(player, "&e&m                                    ");
            MessageUtil.send(player, "&e  📤 &fNhập số lượng cần &eRÚT TỪ KHO");
            MessageUtil.send(player, "&7  Gõ số, 'all' để rút hết, 'cancel' để hủy");
            MessageUtil.send(player, "&e&m                                    ");
            chatWaiting.put(player.getUniqueId(),
                new ActionContext(material, ActionType.WITHDRAW, holder.getTargetUUID()));
            return;
        }

        // Sell (slot 15) - input amount
        if (rawSlot == 15) {
            if (event.getClick() == ClickType.MIDDLE) {
                sellItem(player, holder.getTargetUUID(), material, -1);
            } else {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                MessageUtil.send(player, "&6&m                                    ");
                MessageUtil.send(player, "&6  💰 &fNhập số lượng cần &6BÁN");
                MessageUtil.send(player, "&7  Gõ số, 'all' để bán hết, 'cancel' để hủy");
                MessageUtil.send(player, "&6&m                                    ");
                chatWaiting.put(player.getUniqueId(),
                    new ActionContext(material, ActionType.SELL, holder.getTargetUUID()));
            }
            return;
        }

        // Sell ALL quick (slot 16)
        if (rawSlot == 16) {
            sellItem(player, holder.getTargetUUID(), material, -1);
            return;
        }

        // Back (slot 22)
        if (rawSlot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
            storageGUI.openStorageGUI(player, holder.getTargetUUID());
        }
    }

    // ══════════ SELL CONFIRM ══════════

    private void handleSellConfirm(Player player, StorageHolder holder, InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // Confirm (slot 11)
        if (rawSlot == 11) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            sellAll(player, holder.getTargetUUID());
            return;
        }

        // Cancel (slot 15)
        if (rawSlot == 15) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
            storageGUI.openStorageGUI(player, holder.getTargetUUID());
        }
    }

    // ══════════ SELL LOGIC ══════════

    private void sellItem(Player player, UUID targetUUID, Material material, int requestedAmount) {
        int stored = storageManager.getAmount(targetUUID, material);
        if (stored <= 0) {
            MessageUtil.send(player, "&c❌ Không có vật phẩm trong kho!");
            player.closeInventory();
            return;
        }

        int toSell = (requestedAmount <= 0 || requestedAmount > stored) ? stored : requestedAmount;
        double price = plugin.getConfig().getDouble("Prices." + material.name(), 0.0);
        double totalMoney = price * toSell;

        if (price <= 0) {
            MessageUtil.send(player, "&c❌ Vật phẩm này không thể bán!");
            player.closeInventory();
            return;
        }

        if (economyManager.deposit(player, totalMoney)) {
            storageManager.removeItem(targetUUID, material, toSell);
            String currency = plugin.getConfig().getString("Economy.Currency", "$");

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            MessageUtil.send(player, "&a&m                                    ");
            MessageUtil.send(player, "&a  💰 &fGiao dịch thành công!");
            MessageUtil.send(player, "&8  ▪ &7Đã bán: &f" + MessageUtil.formatNumber(toSell) + " " + material.name());
            MessageUtil.send(player, "&8  ▪ &7Nhận:   &6&l" + MessageUtil.formatNumber(totalMoney) + currency);
            MessageUtil.send(player, "&a&m                                    ");

            logManager.log(player.getUniqueId(), LogManager.Action.SELL, material, toSell,
                "Giá: " + totalMoney + currency);
        } else {
            MessageUtil.send(player, "&c❌ Lỗi Economy! Không thể thực hiện giao dịch.");
        }

        player.closeInventory();
    }

    private void sellAll(Player player, UUID targetUUID) {
        Map<Material, Integer> storage = new HashMap<>(storageManager.getStorage(targetUUID));
        double totalMoney = 0;
        int totalItems = 0;
        int itemTypes = 0;

        for (Map.Entry<Material, Integer> entry : storage.entrySet()) {
            if (entry.getValue() <= 0) continue;
            double price = plugin.getConfig().getDouble("Prices." + entry.getKey().name(), 0.0);
            if (price <= 0) continue;
            totalMoney += price * entry.getValue();
            totalItems += entry.getValue();
            itemTypes++;
        }

        if (totalItems == 0 || totalMoney <= 0) {
            MessageUtil.send(player, "&c❌ Không có vật phẩm nào để bán!");
            player.closeInventory();
            return;
        }

        if (economyManager.deposit(player, totalMoney)) {
            for (Map.Entry<Material, Integer> entry : storage.entrySet()) {
                double price = plugin.getConfig().getDouble("Prices." + entry.getKey().name(), 0.0);
                if (price > 0 && entry.getValue() > 0) {
                    storageManager.removeItem(targetUUID, entry.getKey(), entry.getValue());
                }
            }

            String currency = plugin.getConfig().getString("Economy.Currency", "$");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

            MessageUtil.send(player, "&6&m                                    ");
            MessageUtil.send(player, "&6  💰 &f&lBÁN THÀNH CÔNG!");
            MessageUtil.send(player, "&8  ▪ &7Vật phẩm: &f" + MessageUtil.formatNumber(totalItems) + " &8(" + itemTypes + " loại)");
            MessageUtil.send(player, "&8  ▪ &7Nhận:     &6&l" + MessageUtil.formatNumber(totalMoney) + currency);
            MessageUtil.send(player, "&6&m                                    ");

            logManager.log(player.getUniqueId(), LogManager.Action.SELL, null, totalItems,
                "Sell All: " + totalMoney + currency);
        } else {
            MessageUtil.send(player, "&c❌ Lỗi Economy!");
        }

        player.closeInventory();
    }

    // ══════════ CHAT INPUT ══════════

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!chatWaiting.containsKey(uuid)) return;

        event.setCancelled(true);
        ActionContext ctx = chatWaiting.remove(uuid);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("hủy")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.send(player, "&7✖ Đã hủy thao tác.");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.5f);
            });
            return;
        }

        int amount;
        boolean isAll = message.equalsIgnoreCase("all");

        if (isAll) {
            amount = (ctx.action == ActionType.STORE)
                ? countInInventory(player, ctx.material)
                : storageManager.getAmount(ctx.targetUUID, ctx.material);
        } else {
            try {
                amount = Integer.parseInt(message);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                    MessageUtil.send(player, "&c❌ Số không hợp lệ! Gõ số dương, 'all', hoặc 'cancel'."));
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (ctx.action) {
                case STORE -> doStore(player, ctx.targetUUID, ctx.material, amount);
                case WITHDRAW -> doWithdraw(player, ctx.targetUUID, ctx.material, amount);
                case SELL -> sellItem(player, ctx.targetUUID, ctx.material, amount);
            }
        });
    }

    private void doStore(Player player, UUID targetUUID, Material material, int requestedAmount) {
        int available = countInInventory(player, material);
        if (available <= 0) {
            MessageUtil.send(player, "&c❌ Không có " + material.name() + " trong túi!");
            return;
        }

        int toStore = Math.min(requestedAmount, available);
        int remaining = toStore;

        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() != material) continue;
            int stackAmt = item.getAmount();
            int toRemove = Math.min(stackAmt, remaining);
            item.setAmount(stackAmt - toRemove);
            if (item.getAmount() <= 0) player.getInventory().setItem(i, null);
            remaining -= toRemove;
        }

        int actualStored = toStore - remaining;
        int added = storageManager.addItem(targetUUID, material, actualStored);

        if (added > 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
            MessageUtil.send(player, "&a📦 Đã cất &f" + MessageUtil.formatNumber(added) + " &7" + material.name() + " &avào kho");
            logManager.log(player.getUniqueId(), LogManager.Action.STORE, material, added);

            if (added < actualStored) {
                int refund = actualStored - added;
                player.getInventory().addItem(new ItemStack(material, refund));
                MessageUtil.send(player, "&e⚠ Kho gần đầy! &7" + refund + " đã trả lại túi.");
            }
        } else {
            player.getInventory().addItem(new ItemStack(material, actualStored));
            MessageUtil.send(player, "&c❌ Kho đã đầy!");
        }
    }

    private void doWithdraw(Player player, UUID targetUUID, Material material, int requestedAmount) {
        int stored = storageManager.getAmount(targetUUID, material);
        if (stored <= 0) {
            MessageUtil.send(player, "&c❌ Không có " + material.name() + " trong kho!");
            return;
        }

        int space = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                space += material.getMaxStackSize();
            } else if (item.getType() == material) {
                space += material.getMaxStackSize() - item.getAmount();
            }
        }

        if (space <= 0) {
            MessageUtil.send(player, "&c❌ Túi đồ đã đầy!");
            return;
        }

        int toTake = Math.min(Math.min(requestedAmount, stored), space);

        if (storageManager.removeItem(targetUUID, material, toTake)) {
            player.getInventory().addItem(new ItemStack(material, toTake));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.0f);
            MessageUtil.send(player, "&e📤 Đã rút &f" + MessageUtil.formatNumber(toTake) + " &7" + material.name() + " &etừ kho");
            logManager.log(player.getUniqueId(), LogManager.Action.WITHDRAW, material, toTake);
        } else {
            MessageUtil.send(player, "&c❌ Không đủ vật phẩm!");
        }
    }

    // ══════════ HELPERS ══════════

    private boolean isOreSlot(int slot) {
        return Arrays.asList(10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34).contains(slot);
    }

    private boolean isUiMaterial(Material mat) {
        return mat == Material.BARRIER || mat == Material.PLAYER_HEAD
            || mat == Material.ENDER_CHEST || mat == Material.ARROW;
    }

    private int countInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) count += item.getAmount();
        }
        return count;
    }

    enum ActionType { STORE, WITHDRAW, SELL }

    static class ActionContext {
        final Material material;
        final ActionType action;
        final UUID targetUUID;

        ActionContext(Material material, ActionType action, UUID targetUUID) {
            this.material = material;
            this.action = action;
            this.targetUUID = targetUUID;
        }
    }
}