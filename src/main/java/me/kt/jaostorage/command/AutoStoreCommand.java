package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
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
            sender.sendMessage("Lệnh này chỉ dành cho người chơi.");
            return true;
        }

        Player player = (Player) sender;
        boolean nowEnabled = !plugin.getSettingManager().isAutoStoreEnabled(player);
        plugin.getSettingManager().setAutoStore(player, nowEnabled);

        player.sendMessage("🔁 AutoStore đã " + (nowEnabled ? "§aBẬT" : "§cTẮT") + "!");
        return true;
    }
}
