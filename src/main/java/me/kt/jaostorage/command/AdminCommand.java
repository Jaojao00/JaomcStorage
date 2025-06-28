package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.config.OreConfig;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminCommand implements CommandExecutor {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.setOreConfig(new OreConfig((JavaPlugin) plugin));
            sender.sendMessage(ChatColor.GREEN + "[JaoStorage] Đã reload file guiore.yml!");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Lệnh này chỉ dùng trong game.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            plugin.getStorageGUI().openStorageGUI(player, target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Đã mở kho của " + ChatColor.YELLOW + target.getName());
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            Material mat = Material.matchMaterial(args[2].toUpperCase());
            if (mat == null) {
                sender.sendMessage(ChatColor.RED + "Vật phẩm không hợp lệ!");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Số lượng không hợp lệ!");
                return true;
            }

            plugin.getStorageManager().addItemToPlayerStorage(target.getUniqueId(), mat, amount);
            sender.sendMessage(ChatColor.GREEN + "Đã thêm " + ChatColor.YELLOW + amount + " " + mat.name() +
                    ChatColor.GREEN + " vào kho của " + ChatColor.AQUA + target.getName());
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("remove")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            Material mat = Material.matchMaterial(args[2].toUpperCase());
            if (mat == null) {
                sender.sendMessage(ChatColor.RED + "Vật phẩm không hợp lệ!");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Số lượng không hợp lệ!");
                return true;
            }

            plugin.getStorageManager().removeItemFromPlayerStorage(target.getUniqueId(), mat, amount);
            sender.sendMessage(ChatColor.YELLOW + "Đã xoá " + ChatColor.RED + amount + " " + mat.name() +
                    ChatColor.YELLOW + " khỏi kho của " + ChatColor.AQUA + target.getName());
            return true;
        }

        // Trợ giúp sử dụng lệnh
        sender.sendMessage(ChatColor.RED + "❓ Danh sách lệnh:");
        sender.sendMessage(ChatColor.GRAY + "/jaostorage reload");
        sender.sendMessage(ChatColor.GRAY + "/jaostorage open <player>");
        sender.sendMessage(ChatColor.GRAY + "/jaostorage add <player> <item> <số lượng>");
        sender.sendMessage(ChatColor.GRAY + "/jaostorage remove <player> <item> <số lượng>");
        return true;
    }
}