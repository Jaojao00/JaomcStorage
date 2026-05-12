package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.CoopManager;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lệnh /kho - Quản lý kho cá nhân.
 * Hỗ trợ: mở kho, store, take, coop, open (admin).
 */
public class StorageCommand implements TabExecutor {

    private final Main plugin;
    private final StorageManager storageManager;
    private final CoopManager coopManager;

    public StorageCommand(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.coopManager = plugin.getCoopManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cLệnh này chỉ sử dụng trong game.");
            return true;
        }

        // /kho → mở GUI kho cá nhân
        if (args.length == 0) {
            plugin.getStorageGUI().openStorageGUI(player, player.getUniqueId());
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "open" -> handleOpen(player, args);
            case "store", "cat" -> handleStore(player, args);
            case "take", "rut" -> handleTake(player, args);
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "coop" -> handleCoop(player, args);
            case "info" -> handleInfo(player, args);
            default -> {
                sendHelp(player);
                yield true;
            }
        };
    }

    // ==================== SUB COMMANDS ====================

    private boolean handleOpen(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cSử dụng: /kho open <tên người chơi>");
            return true;
        }

        if (!player.hasPermission("jaostorage.admin")) {
            // Kiểm tra coop
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageUtil.send(player, "&cNgười chơi không online!");
                return true;
            }
            if (!coopManager.hasAccess(player.getUniqueId(), target.getUniqueId())) {
                MessageUtil.send(player, "&cBạn không có quyền truy cập kho của người này!");
                return true;
            }
            plugin.getStorageGUI().openStorageGUI(player, target.getUniqueId());
        } else {
            var target = Bukkit.getOfflinePlayer(args[1]);
            plugin.getStorageGUI().openStorageGUI(player, target.getUniqueId());
            MessageUtil.send(player, "&aĐã mở kho của &e" + target.getName());
        }
        return true;
    }

    private boolean handleStore(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(player, "&cSử dụng: /kho store <vật phẩm> <số lượng|all>");
            return true;
        }

        Material mat = Material.matchMaterial(args[1]);
        if (mat == null) {
            MessageUtil.send(player, "&cVật phẩm không hợp lệ: " + args[1]);
            return true;
        }

        boolean isAll = args[2].equalsIgnoreCase("all");
        int amount;
        try {
            amount = isAll ? Integer.MAX_VALUE : Integer.parseInt(args[2]);
            if (!isAll && amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageUtil.send(player, "&cSố lượng không hợp lệ!");
            return true;
        }

        // Đếm trong inventory
        int available = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() == mat) available += item.getAmount();
        }

        if (available == 0) {
            MessageUtil.send(player, "&cKhông có " + mat.name() + " trong túi!");
            return true;
        }

        int toStore = Math.min(amount, available);
        int remaining = toStore;

        // Xóa từ inventory
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == mat) {
                int stackAmt = item.getAmount();
                int toRemove = Math.min(stackAmt, remaining);
                item.setAmount(stackAmt - toRemove);
                if (item.getAmount() <= 0) contents[i] = null;
                remaining -= toRemove;
            }
        }
        player.getInventory().setContents(contents);

        int actualStored = toStore - remaining;
        int added = storageManager.addItem(player.getUniqueId(), mat, actualStored);

        if (added > 0) {
            MessageUtil.send(player, "&aĐã lưu &e" + MessageUtil.formatNumber(added) + " " + mat.name() + " &avào kho.");
            plugin.getLogManager().log(player.getUniqueId(),
                me.kt.jaostorage.storage.LogManager.Action.STORE, mat, added);
        }
        if (added < actualStored) {
            int refund = actualStored - added;
            player.getInventory().addItem(new ItemStack(mat, refund));
            MessageUtil.send(player, "&eKho đầy! &7" + refund + " đã trả lại.");
        }
        return true;
    }

    private boolean handleTake(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(player, "&cSử dụng: /kho take <vật phẩm> <số lượng|all>");
            return true;
        }

        Material mat = Material.matchMaterial(args[1]);
        if (mat == null) {
            MessageUtil.send(player, "&cVật phẩm không hợp lệ!");
            return true;
        }

        int stored = storageManager.getAmount(player.getUniqueId(), mat);
        if (stored <= 0) {
            MessageUtil.send(player, "&cKhông có " + mat.name() + " trong kho!");
            return true;
        }

        boolean isAll = args[2].equalsIgnoreCase("all");
        int requested;
        try {
            requested = isAll ? stored : Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            MessageUtil.send(player, "&cSố lượng không hợp lệ!");
            return true;
        }

        // Tính chỗ trống
        int space = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                space += mat.getMaxStackSize();
            } else if (item.getType() == mat) {
                space += mat.getMaxStackSize() - item.getAmount();
            }
        }

        int toTake = Math.min(Math.min(requested, stored), space);
        if (toTake <= 0) {
            MessageUtil.send(player, "&cKhông đủ chỗ trong túi!");
            return true;
        }

        if (storageManager.removeItem(player.getUniqueId(), mat, toTake)) {
            player.getInventory().addItem(new ItemStack(mat, toTake));
            MessageUtil.send(player, "&aĐã rút &e" + MessageUtil.formatNumber(toTake) + " " + mat.name() + " &atừ kho.");
            plugin.getLogManager().log(player.getUniqueId(),
                me.kt.jaostorage.storage.LogManager.Action.WITHDRAW, mat, toTake);
        }
        return true;
    }

    private boolean handleAdd(Player player, String[] args) {
        if (!player.hasPermission("jaostorage.admin")) {
            MessageUtil.send(player, "&cBạn không có quyền!");
            return true;
        }
        if (args.length < 4) {
            MessageUtil.send(player, "&cSử dụng: /kho add <player> <item> <amount>");
            return true;
        }

        var target = Bukkit.getOfflinePlayer(args[1]);
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null) { MessageUtil.send(player, "&cVật phẩm không hợp lệ!"); return true; }

        int amount;
        try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) {
            MessageUtil.send(player, "&cSố lượng không hợp lệ!"); return true;
        }

        storageManager.addItem(target.getUniqueId(), mat, amount);
        MessageUtil.send(player, "&aĐã thêm &e" + amount + " " + mat.name() + " &avào kho của &b" + target.getName());
        plugin.getLogManager().log(target.getUniqueId(),
            me.kt.jaostorage.storage.LogManager.Action.ADMIN_ADD, mat, amount, "By: " + player.getName());
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("jaostorage.admin")) {
            MessageUtil.send(player, "&cBạn không có quyền!"); return true;
        }
        if (args.length < 4) {
            MessageUtil.send(player, "&cSử dụng: /kho remove <player> <item> <amount>"); return true;
        }

        var target = Bukkit.getOfflinePlayer(args[1]);
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null) { MessageUtil.send(player, "&cVật phẩm không hợp lệ!"); return true; }

        int amount;
        try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) {
            MessageUtil.send(player, "&cSố lượng không hợp lệ!"); return true;
        }

        storageManager.removeItem(target.getUniqueId(), mat, amount);
        MessageUtil.send(player, "&cĐã xóa &e" + amount + " " + mat.name() + " &ckhỏi kho của &b" + target.getName());
        plugin.getLogManager().log(target.getUniqueId(),
            me.kt.jaostorage.storage.LogManager.Action.ADMIN_REMOVE, mat, amount, "By: " + player.getName());
        return true;
    }

    // ==================== COOP ====================

    private boolean handleCoop(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&6=== Coop Commands ===");
            MessageUtil.send(player, "&e/kho coop add <tên> &7- Thêm thành viên");
            MessageUtil.send(player, "&e/kho coop remove <tên> &7- Xóa thành viên");
            MessageUtil.send(player, "&e/kho coop list &7- Xem danh sách");
            return true;
        }

        String sub = args[1].toLowerCase();

        if (sub.equals("add") && args.length >= 3) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) { MessageUtil.send(player, "&cNgười chơi không online!"); return true; }
            if (target.equals(player)) { MessageUtil.send(player, "&cKhông thể thêm chính mình!"); return true; }

            if (coopManager.addCoop(player.getUniqueId(), target.getUniqueId())) {
                MessageUtil.send(player, "&aĐã thêm &b" + target.getName() + " &avào coop của bạn!");
                MessageUtil.send(target, "&b" + player.getName() + " &ađã chia sẻ kho với bạn! Dùng &e/kho open " + player.getName());
                plugin.getLogManager().log(player.getUniqueId(),
                    me.kt.jaostorage.storage.LogManager.Action.COOP_ADD, "Added: " + target.getName());
            } else {
                MessageUtil.send(player, "&cNgười này đã trong coop rồi!");
            }
            return true;
        }

        if (sub.equals("remove") && args.length >= 3) {
            var target = Bukkit.getOfflinePlayer(args[2]);
            if (coopManager.removeCoop(player.getUniqueId(), target.getUniqueId())) {
                MessageUtil.send(player, "&cĐã xóa &b" + target.getName() + " &ckhỏi coop.");
                plugin.getLogManager().log(player.getUniqueId(),
                    me.kt.jaostorage.storage.LogManager.Action.COOP_REMOVE, "Removed: " + target.getName());
            } else {
                MessageUtil.send(player, "&cNgười này không trong coop!");
            }
            return true;
        }

        if (sub.equals("list")) {
            Set<UUID> members = coopManager.getMembers(player.getUniqueId());
            if (members.isEmpty()) {
                MessageUtil.send(player, "&7Chưa chia sẻ kho với ai.");
            } else {
                MessageUtil.send(player, "&6=== Coop Members (" + members.size() + ") ===");
                for (UUID memberUUID : members) {
                    var member = Bukkit.getOfflinePlayer(memberUUID);
                    String status = member.isOnline() ? "&a●" : "&c●";
                    MessageUtil.send(player, status + " &f" + member.getName());
                }
            }
            return true;
        }

        MessageUtil.send(player, "&cSử dụng: /kho coop <add|remove|list>");
        return true;
    }

    // ==================== INFO ====================

    private boolean handleInfo(Player player, String[] args) {
        UUID target = player.getUniqueId();
        MessageUtil.send(player, "&6=== Thông Tin Kho ===");
        MessageUtil.send(player, "&7Tổng vật phẩm: &f" + MessageUtil.formatNumber(storageManager.getTotalItems(target)));
        MessageUtil.send(player, "&7Dung lượng: &f" +
            (storageManager.isInfinity(target) ? "∞ Vô hạn" :
                MessageUtil.formatNumber(storageManager.getTotalItems(target)) + "/" +
                MessageUtil.formatNumber(plugin.getConfig().getInt("MaxSpace", 100000))));
        MessageUtil.send(player, "&7Slot nâng cấp: &f" + storageManager.getSlots(target));
        MessageUtil.send(player, "&7Coop: &f" + coopManager.getMemberCount(target) + " thành viên");
        MessageUtil.send(player, "&7Auto Store: " +
            (plugin.getSettingManager().isAutoStoreEnabled(player) ? "&aBẬT" : "&cTẮT"));
        return true;
    }

    // ==================== HELP ====================

    private void sendHelp(Player player) {
        MessageUtil.send(player, "&6=== JaoStorage Help ===");
        MessageUtil.send(player, "&e/kho &7- Mở kho cá nhân");
        MessageUtil.send(player, "&e/kho store <item> <số|all> &7- Cất vào kho");
        MessageUtil.send(player, "&e/kho take <item> <số|all> &7- Rút từ kho");
        MessageUtil.send(player, "&e/kho info &7- Xem thông tin kho");
        MessageUtil.send(player, "&e/kho coop &7- Quản lý chia sẻ kho");
        if (player.hasPermission("jaostorage.admin")) {
            MessageUtil.send(player, "&c/kho open <player> &7- Mở kho người khác");
            MessageUtil.send(player, "&c/kho add <player> <item> <amount>");
            MessageUtil.send(player, "&c/kho remove <player> <item> <amount>");
        }
    }

    // ==================== TAB COMPLETE ====================

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("store", "take", "info", "coop"));
            if (sender.hasPermission("jaostorage.admin")) {
                completions.addAll(List.of("open", "add", "remove"));
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("store") || sub.equals("take")) {
                // Gợi ý vật phẩm trong whitelist
                completions.addAll(plugin.getConfig().getStringList("Whitelist"));
            } else if (sub.equals("open") || sub.equals("add") || sub.equals("remove")) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            } else if (sub.equals("coop")) {
                completions.addAll(List.of("add", "remove", "list"));
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("store") || sub.equals("take")) {
                completions.addAll(List.of("1", "10", "32", "64", "all"));
            } else if (sub.equals("add") || sub.equals("remove")) {
                completions.addAll(plugin.getConfig().getStringList("Whitelist"));
            } else if (sub.equals("coop") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                completions.addAll(List.of("1", "10", "32", "64", "100"));
            }
        }

        // Filter
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        return completions;
    }
}