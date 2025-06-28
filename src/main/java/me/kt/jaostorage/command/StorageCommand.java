package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.gui.StorageGUI;
import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StorageCommand implements CommandExecutor {

    private final Main plugin;
    private final StorageManager storageManager;

    public StorageCommand(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cLệnh này chỉ sử dụng trong game.");
            return true;
        }

        // 📦 /kho → mở GUI kho cá nhân
        if (args.length == 0) {
            plugin.getStorageGUI().openStorageGUI(player, player.getUniqueId());
            return true;
        }


        // 🔑 /kho open <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            if (!player.hasPermission("jaostorage.admin")) {
                player.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cKhông tìm thấy người chơi.");
                return true;
            }

            new StorageGUI(plugin).openStorageGUI(player, target.getUniqueId());
            return true;
        }

        // ➕ /kho add <player> <material> <amount>
        if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("jaostorage.admin")) {
                player.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            Material mat = Material.matchMaterial(args[2]);
            int amount;

            if (target == null) {
                player.sendMessage("§cKhông tìm thấy người chơi.");
                return true;
            }
            if (mat == null) {
                player.sendMessage("§cVật phẩm không hợp lệ.");
                return true;
            }
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cSố lượng không hợp lệ.");
                return true;
            }

            storageManager.addItem(target.getUniqueId(), mat, amount);
            player.sendMessage("§aĐã thêm §e" + amount + " " + mat.name() + " §avào kho của §b" + target.getName());
            return true;
        }

        // ➖ /kho remove <player> <material> <amount>
        if (args.length == 4 && args[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("jaostorage.admin")) {
                player.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            Material mat = Material.matchMaterial(args[2]);
            int amount;

            if (target == null) {
                player.sendMessage("§cKhông tìm thấy người chơi.");
                return true;
            }
            if (mat == null) {
                player.sendMessage("§cVật phẩm không hợp lệ.");
                return true;
            }
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cSố lượng không hợp lệ.");
                return true;
            }

            storageManager.removeItem(target.getUniqueId(), mat, amount);
            player.sendMessage("§cĐã xoá §e" + amount + " " + mat.name() + " §ckhỏi kho của §b" + target.getName());
            return true;
        }

        // 🎒 /kho store <material> <amount|all>
        if (args.length == 3 && args[0].equalsIgnoreCase("store")) {
            Material mat = Material.matchMaterial(args[1]);
            int amount;

            if (mat == null) {
                player.sendMessage("§cVật phẩm không hợp lệ.");
                return true;
            }

            boolean all = args[2].equalsIgnoreCase("all");
            try {
                amount = all ? -1 : Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cSố lượng không hợp lệ.");
                return true;
            }

            int stored = 0;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null && item.getType() == mat) {
                    int amt = item.getAmount();
                    if (amount == -1 || stored + amt <= amount) {
                        stored += amt;
                        contents[i] = null;
                    } else {
                        int toStore = amount - stored;
                        item.setAmount(amt - toStore);
                        stored += toStore;
                        break;
                    }
                }
            }

            player.getInventory().setContents(contents);

            if (stored > 0) {
                storageManager.addItem(player.getUniqueId(), mat, stored);
                player.sendMessage("§aĐã lưu §e" + stored + " " + mat.name() + " §avào kho.");
            } else {
                player.sendMessage("§cKhông có vật phẩm phù hợp trong túi.");
            }
            return true;
        }

        // 📤 /kho take <material> <amount|all>
        if (args.length == 3 && args[0].equalsIgnoreCase("take")) {
            Material mat = Material.matchMaterial(args[1]);
            if (mat == null) {
                player.sendMessage("§cVật phẩm không hợp lệ.");
                return true;
            }

            boolean isAll = args[2].equalsIgnoreCase("all");
            int requested;

            try {
                requested = isAll ? -1 : Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cSố lượng không hợp lệ.");
                return true;
            }

            int stored = storageManager.getAmount(player.getUniqueId(), mat);
            if (stored <= 0) {
                player.sendMessage("§cKhông có vật phẩm trong kho.");
                return true;
            }

            int space = 0;
            for (ItemStack item : player.getInventory().getStorageContents()) {
                if (item == null || item.getType() == Material.AIR) {
                    space += mat.getMaxStackSize();
                } else if (item.getType() == mat) {
                    space += mat.getMaxStackSize() - item.getAmount();
                }
            }

            int toTake = Math.min(isAll ? stored : requested, space);
            if (toTake <= 0) {
                player.sendMessage("§cKhông còn chỗ để lấy vật phẩm.");
                return true;
            }

            player.getInventory().addItem(new ItemStack(mat, toTake));
            storageManager.removeItem(player.getUniqueId(), mat, toTake);
            player.sendMessage("§aĐã rút §e" + toTake + " " + mat.name() + " §atừ kho.");
            return true;
        }

        player.sendMessage("§6Sử dụng: /kho, /kho store, /kho take, /kho open, add, remove");
        return true;
    }
}