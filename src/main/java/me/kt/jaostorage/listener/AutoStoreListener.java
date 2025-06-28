package me.kt.jaostorage.listener;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.storage.SettingManager;
import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class AutoStoreListener implements Listener {

    private final Main plugin;
    private final StorageManager storageManager;
    private final SettingManager settingManager;

    public AutoStoreListener(Main plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.settingManager = plugin.getSettingManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (!settingManager.getBoolean("PickupToStorage")) return;
        if (settingManager.getStringList("BlacklistWorlds").contains(block.getWorld().getName())) return;
        if (!settingManager.isAutoStoreEnabled(player)) return;

        Material dropType = getDropFor(blockType);
        if (dropType == null || dropType == Material.AIR) return;

        List<String> blacklist = settingManager.getStringList("Blacklist");
        List<String> whitelist = settingManager.getStringList("Whitelist");

        if (blacklist.contains(dropType.name())) return;
        if (!whitelist.isEmpty() && !whitelist.contains(dropType.name())) return;
        if (settingManager.getBoolean("OnlyStoreWhenInvFull") && player.getInventory().firstEmpty() != -1) return;

        int fortune = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.FORTUNE);
        int amount = getFortuneAmount(fortune);
        int xp = getXpWithFortune(blockType, fortune);

        block.setType(Material.AIR);
        event.setDropItems(false);
        storageManager.addItem(player.getUniqueId(), dropType, amount);

        if (xp > 0) player.giveExp(xp);
        playPickupSound(player);
        sendActionBar(player, dropType, amount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!settingManager.getBoolean("PickupToStorage")) return;
        if (!settingManager.isAutoStoreEnabled(player)) return;
        if (settingManager.getBoolean("OnlyStoreWhenInvFull") && player.getInventory().firstEmpty() != -1) return;

        event.setCancelled(true);
        event.getItem().remove();

        ItemStack item = event.getItem().getItemStack();
        Material dropType = item.getType();
        int amount = item.getAmount();

        storageManager.addItem(player.getUniqueId(), dropType, amount);
        playPickupSound(player);
        sendActionBar(player, dropType, amount);
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
            case NETHER_GOLD_ORE -> Material.GOLD_NUGGET;
            case STONE -> Material.COBBLESTONE;
            default -> Material.AIR;
        };
    }

    private int getFortuneAmount(int fortuneLevel) {
        if (fortuneLevel <= 0) return 1;
        int bonus = new Random().nextInt(fortuneLevel + 2) - 1;
        return 1 + Math.max(bonus, 0);
    }

    private int getXpWithFortune(Material mat, int fortune) {
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
        int bonus = (fortune > 0) ? new Random().nextInt(fortune + 1) : 0;
        return baseXp + bonus;
    }

    private void playPickupSound(Player player) {
        try {
            String soundName = settingManager.getString("PickupSound");
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {
        }
    }

    private void sendActionBar(Player player, Material material, int amount) {
        if (!plugin.getConfig().getBoolean("UseActionBar")) return;

        String message = "§a+ " + amount + " " + material.name();
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
    }
}
