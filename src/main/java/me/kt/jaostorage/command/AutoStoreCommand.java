package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Lệnh /autostore - Bật/tắt tự động lưu kho nhanh.
 */
public class AutoStoreCommand implements CommandExecutor {

    private final Main plugin;

    public AutoStoreCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cLệnh này chỉ dành cho người chơi trong game.");
            return true;
        }

        if (!player.hasPermission("jaostorage.autostore")) {
            MessageUtil.send(player, "&cBạn không có quyền dùng lệnh này!");
            return true;
        }

        boolean newState = plugin.getSettingManager().toggleAutoStore(player);
        MessageUtil.send(player, "⚡ Tự động lưu kho: " + (newState ? "&aBẬT" : "&cTẮT"));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        return true;
    }
}
