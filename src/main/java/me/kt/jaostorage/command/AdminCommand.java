package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.config.OreConfig;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Lệnh /jaostorage - Quản trị plugin.
 * Hỗ trợ: reload, open, add, remove, infinity, info, cleanup.
 */
public class AdminCommand implements TabExecutor {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jaostorage.admin")) {
            sender.sendMessage("§cBạn không có quyền thực hiện lệnh này!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "reload" -> handleReload(sender);
            case "open" -> handleOpen(sender, args);
            case "add" -> handleModify(sender, args, true);
            case "remove" -> handleModify(sender, args, false);
            case "infinity" -> handleInfinity(sender, args);
            case "info" -> handleInfo(sender, args);
            case "cleanup" -> handleCleanup(sender, args);
            default -> { sendHelp(sender); yield true; }
        };
    }

    private boolean handleReload(CommandSender sender) {
        // Reload cả config.yml VÀ guiore.yml
        plugin.reloadConfig();
        plugin.setOreConfig(new OreConfig(plugin));

        sender.sendMessage("§a[JaoStorage] ✅ Đã reload config.yml + guiore.yml!");
        return true;
    }

    private boolean handleOpen(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cLệnh này chỉ dùng trong game.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /jaostorage open <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        plugin.getStorageGUI().openStorageGUI(player, target.getUniqueId());
        MessageUtil.send(player, "&aĐã mở kho của &e" + target.getName());
        return true;
    }

    private boolean handleModify(CommandSender sender, String[] args, boolean isAdd) {
        if (args.length < 4) {
            sender.sendMessage("§cSử dụng: /jaostorage " + (isAdd ? "add" : "remove") + " <player> <item> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Material mat = Material.matchMaterial(args[2].toUpperCase());
        if (mat == null) {
            sender.sendMessage("§cVật phẩm không hợp lệ: " + args[2]);
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage("§cSố lượng không hợp lệ!");
            return true;
        }

        if (isAdd) {
            int added = plugin.getStorageManager().addItem(target.getUniqueId(), mat, amount);
            sender.sendMessage("§aĐã thêm §e" + added + " " + mat.name() + " §avào kho của §b" + target.getName());
            plugin.getLogManager().log(target.getUniqueId(),
                me.kt.jaostorage.storage.LogManager.Action.ADMIN_ADD, mat, added,
                "By: " + sender.getName());
        } else {
            plugin.getStorageManager().removeItem(target.getUniqueId(), mat, amount);
            sender.sendMessage("§cĐã xóa §e" + amount + " " + mat.name() + " §ckhỏi kho của §b" + target.getName());
            plugin.getLogManager().log(target.getUniqueId(),
                me.kt.jaostorage.storage.LogManager.Action.ADMIN_REMOVE, mat, amount,
                "By: " + sender.getName());
        }
        return true;
    }

    private boolean handleInfinity(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /jaostorage infinity <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean newState = plugin.getStorageManager().toggleInfinity(target.getUniqueId());
        sender.sendMessage("§eKho vô hạn cho §b" + target.getName() + "§e: " + (newState ? "§aBẬT" : "§cTẮT"));
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /jaostorage info <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        sender.sendMessage("§6=== Kho của " + target.getName() + " ===");
        sender.sendMessage("§7Tổng items: §f" + MessageUtil.formatNumber(plugin.getStorageManager().getTotalItems(uuid)));
        sender.sendMessage("§7MaxSpace: §f" + MessageUtil.formatNumber(plugin.getConfig().getInt("MaxSpace")));
        sender.sendMessage("§7Infinity: " + (plugin.getStorageManager().isInfinity(uuid) ? "§aCÓ" : "§cKHÔNG"));
        sender.sendMessage("§7Slots nâng cấp: §f" + plugin.getStorageManager().getSlots(uuid));
        sender.sendMessage("§7AutoStore: " + (plugin.getSettingManager().isAutoStoreEnabled(uuid) ? "§aBẬT" : "§cTẮT"));
        sender.sendMessage("§7Coop members: §f" + plugin.getCoopManager().getMemberCount(uuid));

        // Hiển thị top items
        var storage = plugin.getStorageManager().getStorage(uuid);
        if (!storage.isEmpty()) {
            sender.sendMessage("§6--- Vật phẩm ---");
            storage.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Material, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> sender.sendMessage("  §7" + e.getKey().name() + ": §f" + MessageUtil.formatNumber(e.getValue())));
        }
        return true;
    }

    private boolean handleCleanup(CommandSender sender, String[] args) {
        int days = 30;
        if (args.length >= 2) {
            try { days = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }
        plugin.getLogManager().cleanOldLogs(days);
        sender.sendMessage("§a[JaoStorage] Đang dọn log cũ hơn " + days + " ngày...");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== JaoStorage Admin ===");
        sender.sendMessage("§e/jaostorage reload §7- Reload config");
        sender.sendMessage("§e/jaostorage open <player> §7- Mở kho");
        sender.sendMessage("§e/jaostorage add <player> <item> <amount>");
        sender.sendMessage("§e/jaostorage remove <player> <item> <amount>");
        sender.sendMessage("§e/jaostorage infinity <player> §7- Toggle vô hạn");
        sender.sendMessage("§e/jaostorage info <player> §7- Xem thông tin");
        sender.sendMessage("§e/jaostorage cleanup [days] §7- Dọn log cũ");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("reload", "open", "add", "remove", "infinity", "info", "cleanup"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (List.of("open", "add", "remove", "infinity", "info").contains(sub)) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            } else if (sub.equals("cleanup")) {
                completions.addAll(List.of("7", "14", "30", "60", "90"));
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            completions.addAll(plugin.getConfig().getStringList("Whitelist"));
        } else if (args.length == 4 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            completions.addAll(List.of("1", "10", "64", "100", "1000"));
        }

        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        return completions;
    }
}