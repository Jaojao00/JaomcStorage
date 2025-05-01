package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.gui.StorageGUI;
import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageCommand implements CommandExecutor {

    private final StorageManager storageManager;

    public StorageCommand(Main plugin) {
        this.storageManager = plugin.getStorageManager();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // ✅ Chỉ cho phép người chơi thực hiện
        if (!(sender instanceof Player player)) {
            sender.sendMessage("❌ Lệnh này chỉ dùng trong game!");
            return true;
        }

        // 👉 /kho
        if (args.length == 0) {
            if (storageManager.getStorage(player.getUniqueId()) == null) {
                player.sendMessage("⚠ Chưa có dữ liệu kho! Hãy thử relog hoặc báo cho admin.");
                return true;
            }

            StorageGUI.open(player);
            return true;
        }

        // 👉 /kho upgrade <player> <slot>
        if (args[0].equalsIgnoreCase("upgrade")) {
            if (args.length != 3) {
                player.sendMessage("❌ Dùng: /kho upgrade <player> <slot>");
                return true;
            }

            Player target = getOnlinePlayer(player, args[1]);
            if (target == null) return true;

            try {
                int slots = Integer.parseInt(args[2]);
                storageManager.addStorageSlots(target.getUniqueId(), slots);
                player.sendMessage("✅ Đã tăng " + slots + " slot cho " + target.getName());
            } catch (NumberFormatException e) {
                player.sendMessage("❌ Số lượng không hợp lệ!");
            }
            return true;
        }

        // 👉 /kho setinfinity <player>
        if (args[0].equalsIgnoreCase("setinfinity")) {
            if (args.length != 2) {
                player.sendMessage("❌ Dùng: /kho setinfinity <player>");
                return true;
            }

            Player target = getOnlinePlayer(player, args[1]);
            if (target == null) return true;

            boolean isNowInfinity = storageManager.toggleInfinity(target.getUniqueId());
            player.sendMessage("✅ Đã " + (isNowInfinity ? "BẬT" : "TẮT") + " kho vô hạn cho " + target.getName());
            return true;
        }

        // ❌ Sai cú pháp
        player.sendMessage("⚠ Dùng: /kho, /kho upgrade <player> <slot>, /kho setinfinity <player>");
        return true;
    }

    private @Nullable Player getOnlinePlayer(Player sender, String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            sender.sendMessage("❌ Không tìm thấy người chơi: " + name);
            return null;
        }
        if (storageManager.getStorage(target.getUniqueId()) == null) {
            sender.sendMessage("⚠ Người chơi này chưa có dữ liệu kho!");
            return null;
        }
        return target;
    }
}
