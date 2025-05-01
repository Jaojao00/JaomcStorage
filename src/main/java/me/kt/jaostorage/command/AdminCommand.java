package me.kt.jaostorage.command;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.config.OreConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class AdminCommand implements CommandExecutor {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.setOreConfig(new OreConfig(plugin)); // ✅ Reload config
            sender.sendMessage(ChatColor.GREEN + "[JaoStorage] Đã reload guiore.yml!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "❌ Dùng: /jaostorage reload");
        return true;
    }
}
