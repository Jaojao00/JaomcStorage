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

public class StorageGUIListener implements Listener {

    private final Main plugin;
    private final StorageManager storageManager;
    private final StorageGUI storageGUI;

    // Dùng để tạm lưu trạng thái chờ nhập liệu từ chat
    private final Map<Player, ActionContext> chatWaiting = new HashMap<>();

    public StorageGUIListener(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.storageGUI = plugin.getStorageGUI();
    }

    // 🎯 Khi người chơi click vào vật phẩm trong kho
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        // Chỉ xử lý GUI liên quan kho
        String title = e.getView().getTitle();
        if (!title.contains("Khoáng sản") && !title.contains("⚙")) return;

        // Huỷ thao tác mặc định (ngăn lấy vật phẩm ra)
        e.setCancelled(true);

        // Bỏ qua nếu không có vật phẩm
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        // Bỏ qua nếu là kính viền
        Material clickedType = e.getCurrentItem().getType();
        if (clickedType.name().contains("GLASS_PANE")) return;

        // 👉 Nếu đang ở GUI chính (danh sách khoáng sản)
        if (title.contains("Khoáng sản")) {
            storageGUI.openItemMenu(player, clickedType);
            return;
        }

        // 👉 Nếu đang ở GUI thao tác (⚙ loại khoáng sản)
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
                int amount = storageManager.getAmount(player, material);
                if (amount > 0) {
                    storageManager.setAmount(player, material, 0);
                    player.sendMessage("💰 Đã bán toàn bộ " + material.name() + " với số lượng " + amount);
                } else {
                    player.sendMessage("❌ Không có " + material.name() + " trong kho!");
                }
                player.closeInventory();
            }
        }
    }

    // 🧾 Xử lý khi người chơi nhập số lượng từ chat
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
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
                amount = storageManager.getAmount(player, context.material);
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

                // ✅ Tính tổng số lượng vật phẩm trong túi
                int totalAvailable = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == context.material) {
                        totalAvailable += item.getAmount();
                    }
                }

                // ❌ Nếu không có vật phẩm để cất
                if (totalAvailable == 0) {
                    player.sendMessage("❌ Bạn không có vật phẩm " + context.material.name() + " trong túi để cất!");
                    return;
                }

                // 📦 Cất tối đa bằng số lượng có trong túi
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

                storageManager.addItem(player, context.material, toStore);
                player.sendMessage("📦 Đã cất " + toStore + " " + context.material.name() + " vào kho.");

            } else {
                // ✅ Rút vật phẩm: chỉ rút vừa đủ inventory
                int storedAmount = storageManager.getAmount(player, context.material);
                if (storedAmount <= 0) {
                    player.sendMessage("❌ Không có " + context.material.name() + " trong kho!");
                    return;
                }

                // 🔍 Tính số slot trống
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

                boolean success = storageManager.removeItem(player, context.material, withdrawAmount);
                if (success) {
                    while (withdrawAmount > 0) {
                        int toGive = Math.min(withdrawAmount, maxStackSize);
                        player.getInventory().addItem(new ItemStack(context.material, toGive));
                        withdrawAmount -= toGive;
                    }
                    player.sendMessage("📤 Đã rút vật phẩm từ kho vào túi (vừa đủ túi đồ).");
                } else {
                    player.sendMessage("❌ Không đủ " + context.material.name() + " để rút!");
                }
            }
        });
    }

    // 🧠 Enum & Context phụ trợ
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
