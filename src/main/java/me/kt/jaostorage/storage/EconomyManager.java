package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * Quản lý tích hợp Vault Economy.
 * Xử lý tất cả giao dịch tiền tệ (bán, mua, nâng cấp).
 */
public class EconomyManager {

    private final Main plugin;
    private Economy economy;
    private boolean enabled = false;

    public EconomyManager(Main plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("⚠️ Vault không được cài đặt! Tính năng Economy bị tắt.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("⚠️ Không tìm thấy Economy provider! Hãy cài Essentials hoặc CMI.");
            return;
        }

        economy = rsp.getProvider();
        enabled = true;
        plugin.getLogger().info("✅ Kết nối Vault Economy thành công! Provider: " + economy.getName());
    }

    public boolean isEnabled() {
        return enabled && economy != null;
    }

    /**
     * Cộng tiền cho player.
     * @return true nếu thành công
     */
    public boolean deposit(Player player, double amount) {
        if (!isEnabled()) {
            player.sendMessage("§c[JaoStorage] Hệ thống Economy chưa được cài đặt!");
            return false;
        }

        EconomyResponse response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.WARNING, "❌ Lỗi deposit cho " + player.getName() + ": " + response.errorMessage);
            return false;
        }
        return true;
    }

    /**
     * Trừ tiền player.
     * @return true nếu thành công
     */
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled()) return false;

        if (!hasEnough(player, amount)) return false;

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    /**
     * Kiểm tra player có đủ tiền không.
     */
    public boolean hasEnough(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }

    /**
     * Lấy số dư của player.
     */
    public double getBalance(Player player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }

    /**
     * Lấy ký hiệu tiền tệ từ config.
     */
    public String getCurrencySymbol() {
        return plugin.getConfig().getString("Economy.Currency", "$");
    }

    /**
     * Format số tiền.
     */
    public String format(double amount) {
        if (isEnabled()) {
            return economy.format(amount);
        }
        return getCurrencySymbol() + String.format("%.2f", amount);
    }

    public Economy getEconomy() {
        return economy;
    }
}
