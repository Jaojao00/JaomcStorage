package me.kt.jaostorage.gui;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageGUIListener implements Listener {

    private final Main plugin;
    private final StorageManager storageManager;
    private final StorageGUI storageGUI;

    private final Map<Player, ActionContext> chatWaiting = new HashMap<>();

    public StorageGUIListener(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.storageGUI = plugin.getStorageGUI();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        UUID uuid = player.getUniqueId();

        String title = e.getView().getTitle();
        if (!title.contains("Khoáng sản") && !title.contains("⚙")) return;

        e.setCancelled(true);

        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        Material clickedType = e.getCurrentItem().getType();
        if (clickedType.name().contains("GLASS_PANE")) return;

        if (title.contains("Khoáng sản")) {
            int rawSlot = e.getRawSlot();
            if (rawSlot == 45) {
                // 🔘 Bật/tắt AutoStore
                boolean current = plugin.getSettingManager().isAutoStoreEnabled(player);
                plugin.getSettingManager().setAutoStore(player, !current);
                player.sendMessage("⚙ Đã " + (current ? "§ctắt" : "§abật") + " chức năng Tự động lưu kho.");
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        storageGUI.openStorageGUI(player, player.getUniqueId()), 1L);
            } else {
                storageGUI.openItemMenu(player, clickedType);
            }
            return;
        }

        if (title.contains("⚙")) {
            String displayName = e.getCurrentItem().getItemMeta() != null
                    ? e.getCurrentItem().getItemMeta().getDisplayName()
                    : "";

            String matName = title.replace("⚙ ", "").trim();
            Material material = Material.matchMaterial(matName.toUpperCase());

            if (material == null) {
                player.sendMessage("❌ Không xác định được loại vật phẩm!");
                return;
            }

            if (displayName.contains("Cất")) {
                player.closeInventory();
                player.sendMessage("✏ Nhập số lượng cần CẤT vào kho hoặc gõ 'all':");
                chatWaiting.put(player, new ActionContext(material, ActionType.STORE));

            } else if (displayName.contains("Rút")) {
                player.closeInventory();
                player.sendMessage("✏ Nhập số lượng cần RÚT khỏi kho hoặc gõ 'all':");
                chatWaiting.put(player, new ActionContext(material, ActionType.WITHDRAW));

            } else if (displayName.contains("Bán")) {
                int amount = storageManager.getAmount(uuid, material);
                if (amount > 0) {
                    storageManager.setAmount(uuid, material, 0);
                    player.sendMessage("💰 Đã bán toàn bộ " + material.name() + " với số lượng " + amount);
                } else {
                    player.sendMessage("❌ Không có " + material.name() + " trong kho!");
                }
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!chatWaiting.containsKey(player)) return;

        e.setCancelled(true);
        ActionContext context = chatWaiting.remove(player);
        String message = e.getMessage();

        int amount;
        if (message.equalsIgnoreCase("all")) {
            if (context.action == ActionType.STORE) {
                amount = (int) player.getInventory().all(context.material).values().stream()
                        .mapToInt(ItemStack::getAmount).sum();
            } else {
                amount = storageManager.getAmount(uuid, context.material);
            }
        } else {
            try {
                amount = Integer.parseInt(message);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                player.sendMessage("❌ Số lượng không hợp lệ!");
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (context.action == ActionType.STORE) {
                int totalAvailable = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == context.material) {
                        totalAvailable += item.getAmount();
                    }
                }

                if (totalAvailable == 0) {
                    player.sendMessage("❌ Bạn không có " + context.material.name() + " trong túi để cất!");
                    return;
                }

                int toStore = Math.min(amount, totalAvailable);
                int remaining = toStore;

                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item == null || item.getType() != context.material) continue;

                    int stackAmount = item.getAmount();
                    int toRemove = Math.min(stackAmount, remaining);
                    item.setAmount(stackAmount - toRemove);

                    if (item.getAmount() <= 0) {
                        player.getInventory().setItem(i, null);
                    }

                    remaining -= toRemove;
                    if (remaining <= 0) break;
                }

                // ✅ Dùng method chính xác có lưu data
                storageManager.addItem(uuid, context.material, toStore);
                player.sendMessage("📦 Đã cất " + toStore + " " + context.material.name() + " vào kho.");

            } else {
                int storedAmount = storageManager.getAmount(uuid, context.material);
                if (storedAmount <= 0) {
                    player.sendMessage("❌ Không có " + context.material.name() + " trong kho!");
                    return;
                }

                int emptySlots = 0;
                for (ItemStack item : player.getInventory().getStorageContents()) {
                    if (item == null || item.getType() == Material.AIR) emptySlots++;
                }

                int maxStackSize = context.material.getMaxStackSize();
                int maxWithdrawable = emptySlots * maxStackSize;

                if (maxWithdrawable <= 0) {
                    player.sendMessage("❌ Túi đồ của bạn đã đầy. Không thể rút thêm.");
                    return;
                }

                int withdrawAmount = Math.min(amount, Math.min(storedAmount, maxWithdrawable));

                boolean success = storageManager.removeItem(uuid, context.material, withdrawAmount);
                if (success) {
                    while (withdrawAmount > 0) {
                        int toGive = Math.min(withdrawAmount, maxStackSize);
                        player.getInventory().addItem(new ItemStack(context.material, toGive));
                        withdrawAmount -= toGive;
                    }
                    player.sendMessage("📤 Đã rút " + amount + " " + context.material.name() + " từ kho.");
                } else {
                    player.sendMessage("❌ Không đủ vật phẩm để rút!");
                }
            }
        });
    }

    enum ActionType { STORE, WITHDRAW }

    class ActionContext {
        Material material;
        ActionType action;

        ActionContext(Material material, ActionType action) {
            this.material = material;
            this.action = action;
        }
    }
}