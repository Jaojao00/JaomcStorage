package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoStoreCommand implements CommandExecutor {

    private final Main plugin;

    public AutoStoreCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cLệnh này chỉ dành cho người chơi trong game.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("jaostorage.autostore")) {
            player.sendMessage("§cBạn không có quyền dùng lệnh này!");
            return true;
        }

        boolean nowEnabled = !plugin.getSettingManager().isAutoStoreEnabled(player);
        plugin.getSettingManager().setAutoStore(player, nowEnabled);

        player.sendMessage("§eTự động lưu vật phẩm: " + (nowEnabled ? "§aBẬT" : "§cTẮT"));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        return true;
    }
}
