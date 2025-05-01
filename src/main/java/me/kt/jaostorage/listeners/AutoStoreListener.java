package me.kt.jaostorage.listeners;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.storage.SettingManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class AutoStoreListener implements Listener {

    private final StorageManager storageManager;
    private final Main plugin;

    public AutoStoreListener(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    /**
     * ✅ Sự kiện khi người chơi phá block (đào quặng)
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Material blockType = block.getType();

        boolean pickupToStorage = SettingManager.getBoolean("PickupToStorage");
        boolean onlyStoreWhenInvFull = SettingManager.getBoolean("OnlyStoreWhenInvFull");
        List<String> blacklistWorlds = SettingManager.getStringList("BlacklistWorlds");
        List<String> blacklist = SettingManager.getStringList("Blacklist");
        List<String> whitelist = SettingManager.getStringList("Whitelist");

        if (!pickupToStorage || blacklistWorlds.contains(block.getWorld().getName())) return;

        Material dropType = getDropFor(blockType);
        if (dropType == null) return;

        if (blacklist.contains(dropType.name())) return;
        if (!whitelist.isEmpty() && !whitelist.contains(dropType.name())) return;
        if (onlyStoreWhenInvFull && player.getInventory().firstEmpty() != -1) return;

        int amount = getFortuneAmount(player);

        storageManager.addItem(player, dropType, amount);
        e.setDropItems(false);
        block.setType(Material.AIR);

        playPickupSound(player);

        // ✅ Sử dụng Adventure API để hiển thị action bar
        if (plugin.getConfig().getBoolean("UseActionBar")) {
            plugin.adventure().player(player).sendActionBar(
                    Component.text("⛏ + " + amount + " " + dropType.name())
            );
        }
    }

    /**
     * ✅ Sự kiện khi người chơi nhặt item rơi trên mặt đất
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        boolean pickupToStorage = SettingManager.getBoolean("PickupToStorage");
        if (!pickupToStorage) return;

        ItemStack item = e.getItem().getItemStack();
        Material type = item.getType();
        int amount = item.getAmount();

        List<String> blacklist = SettingManager.getStringList("Blacklist");
        List<String> whitelist = SettingManager.getStringList("Whitelist");

        if (blacklist.contains(type.name())) return;
        if (!whitelist.isEmpty() && !whitelist.contains(type.name())) return;
        if (SettingManager.getBoolean("OnlyStoreWhenInvFull") && player.getInventory().firstEmpty() != -1) return;

        storageManager.addItem(player, type, amount);
        e.getItem().remove();
        e.setCancelled(true);

        playPickupSound(player);

        // ✅ Sửa lỗi thiếu biến dropType → dùng chính `type`
        if (plugin.getConfig().getBoolean("UseActionBar")) {
            plugin.adventure().player(player).sendActionBar(
                    Component.text("⛏ + " + amount + " " + type.name())
            );
        }
    }

    private Material getDropFor(Material ore) {
        return switch (ore) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> Material.COAL;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.RAW_IRON;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.RAW_COPPER;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.RAW_GOLD;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_LAZULI;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD;
            case NETHER_QUARTZ_ORE -> Material.QUARTZ;
            case STONE -> Material.COBBLESTONE;
            default -> null;
        };
    }

    private int getFortuneAmount(Player player) {
        int level = player.getInventory().getItemInMainHand()
                .getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        if (level <= 0) return 1;

        Random random = new Random();
        int bonus = random.nextInt(level + 2) - 1;
        return 1 + Math.max(bonus, 0);
    }

    private void playPickupSound(Player player) {
        try {
            String soundName = SettingManager.getString("PickupSound");
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
            // Nếu cấu hình sai sound → bỏ qua
        }
    }
}