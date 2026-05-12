package me.kt.jaostorage.listener;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.LogManager;
import me.kt.jaostorage.storage.SettingManager;
import me.kt.jaostorage.storage.StorageManager;
import me.kt.jaostorage.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener xử lý AutoStore khi đào block và nhặt item.
 * Tích hợp: MaxSpace, BlockedMining, Infinity, Fortune, XP, Blacklist/Whitelist.
 */
public class AutoStoreListener implements Listener {

    private final Main plugin;
    private final StorageManager storageManager;
    private final SettingManager settingManager;
    private final LogManager logManager;

    public AutoStoreListener(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.settingManager = plugin.getSettingManager();
        this.logManager = plugin.getLogManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Kiểm tra điều kiện cơ bản
        if (!settingManager.getBoolean("PickupToStorage")) return;
        if (settingManager.getStringList("BlacklistWorlds").contains(block.getWorld().getName())) return;
        if (!settingManager.isAutoStoreEnabled(player)) return;

        // Xác định drop item
        Material dropType = getDropFor(blockType);
        if (dropType == null || dropType == Material.AIR) return;

        // Blacklist / Whitelist check
        List<String> blacklist = settingManager.getStringList("Blacklist");
        List<String> whitelist = settingManager.getStringList("Whitelist");
        if (blacklist.contains(dropType.name())) return;
        if (!whitelist.isEmpty() && !whitelist.contains(dropType.name())) return;

        // OnlyStoreWhenInvFull check
        if (settingManager.getBoolean("OnlyStoreWhenInvFull") && player.getInventory().firstEmpty() != -1) return;

        // ⛔ BlockedMining: chặn đào nếu kho đầy
        if (settingManager.getBoolean("BlockedMining") && storageManager.isFull(player.getUniqueId())) {
            event.setCancelled(true);
            MessageUtil.sendActionBar(player, "&c⛔ Kho đã đầy! Hãy bán hoặc nâng cấp kho.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
            return;
        }

        // Tính Fortune
        int fortune = 0;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.containsEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE)) {
            fortune = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.FORTUNE);
        }
        int amount = getFortuneAmount(fortune);
        int xp = getXpDrop(blockType, fortune);

        // Thêm vào kho (có kiểm tra MaxSpace)
        int stored = storageManager.addItem(player.getUniqueId(), dropType, amount);

        if (stored <= 0) {
            // Kho đã đầy - để block rơi bình thường
            MessageUtil.sendActionBar(player, "&c⚠ Kho đầy! Vật phẩm sẽ rơi ra đất.");
            return;
        }

        // Xóa block và ngăn drop mặc định
        block.setType(Material.AIR);
        event.setDropItems(false);

        // Cho XP
        if (xp > 0) player.giveExp(xp);

        // Hiệu ứng
        playPickupSound(player);
        sendStoreActionBar(player, dropType, stored);

        // Log
        logManager.log(player.getUniqueId(), LogManager.Action.AUTO_STORE, dropType, stored);

        // Nếu chỉ lưu được 1 phần (kho gần đầy)
        if (stored < amount) {
            int leftover = amount - stored;
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType, leftover));
            MessageUtil.send(player, "&eKho gần đầy! &7" + leftover + " " + dropType.name() + " rơi ra đất.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!settingManager.getBoolean("PickupToStorage")) return;
        if (!settingManager.isAutoStoreEnabled(player)) return;
        if (settingManager.getBoolean("OnlyStoreWhenInvFull") && player.getInventory().firstEmpty() != -1) return;

        ItemStack item = event.getItem().getItemStack();
        Material dropType = item.getType();
        int amount = item.getAmount();

        // Chỉ auto-store item trong whitelist
        List<String> whitelist = settingManager.getStringList("Whitelist");
        List<String> blacklist = settingManager.getStringList("Blacklist");
        if (blacklist.contains(dropType.name())) return;
        if (!whitelist.isEmpty() && !whitelist.contains(dropType.name())) return;

        int stored = storageManager.addItem(player.getUniqueId(), dropType, amount);
        if (stored <= 0) return; // Kho đầy, để nhặt bình thường vào inventory

        event.setCancelled(true);

        if (stored >= amount) {
            // Lưu hết
            event.getItem().remove();
        } else {
            // Lưu 1 phần
            item.setAmount(amount - stored);
            event.getItem().setItemStack(item);
        }

        playPickupSound(player);
        sendStoreActionBar(player, dropType, stored);
        logManager.log(player.getUniqueId(), LogManager.Action.AUTO_STORE, dropType, stored);
    }

    // ==================== UTILITY ====================

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
            case NETHER_GOLD_ORE -> Material.GOLD_NUGGET;
            case STONE -> Material.COBBLESTONE;
            default -> Material.AIR;
        };
    }

    private int getFortuneAmount(int fortuneLevel) {
        if (fortuneLevel <= 0) return 1;
        int bonus = ThreadLocalRandom.current().nextInt(fortuneLevel + 2) - 1;
        return 1 + Math.max(bonus, 0);
    }

    private int getXpDrop(Material mat, int fortune) {
        int baseXp = switch (mat) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> 1;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> 3;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 3;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> 2;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> 2;
            case NETHER_QUARTZ_ORE -> 2;
            case NETHER_GOLD_ORE -> 1;
            default -> 0;
        };
        int bonus = (fortune > 0) ? ThreadLocalRandom.current().nextInt(fortune + 1) : 0;
        return baseXp + bonus;
    }

    private void playPickupSound(Player player) {
        try {
            String soundName = settingManager.getString("PickupSound");
            if (!soundName.isEmpty()) {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
            }
        } catch (Exception ignored) {}
    }

    private void sendStoreActionBar(Player player, Material material, int amount) {
        if (!plugin.getConfig().getBoolean("UseActionBar", true)) return;

        String formatName = plugin.getConfig().getString("FormatName." + material.name(), material.name());
        formatName = MessageUtil.color(formatName);

        int total = storageManager.getAmount(player.getUniqueId(), material);
        int remaining = storageManager.getRemainingSpace(player.getUniqueId());

        String capacityInfo = storageManager.isInfinity(player.getUniqueId())
                ? "&a∞" : "&7" + MessageUtil.formatNumber(remaining) + " còn lại";

        MessageUtil.sendActionBar(player,
                "&a+ " + amount + " " + formatName + " &8| &7Tổng: &f" + MessageUtil.formatNumber(total) +
                " &8| " + capacityInfo);
    }
}
