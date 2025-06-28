package me.kt.jaostorage.listener;

import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockBreakListener implements Listener {

    private final StorageManager storageManager;
    private final List<Material> whitelistItems;

    private static final Map<Material, Material> DROP_MAP = new HashMap<>();
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();

    static {
        DROP_MAP.put(Material.COAL_ORE, Material.COAL);
        DROP_MAP.put(Material.IRON_ORE, Material.RAW_IRON);
        DROP_MAP.put(Material.COPPER_ORE, Material.RAW_COPPER);
        DROP_MAP.put(Material.GOLD_ORE, Material.RAW_GOLD);
        DROP_MAP.put(Material.REDSTONE_ORE, Material.REDSTONE);
        DROP_MAP.put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
        DROP_MAP.put(Material.EMERALD_ORE, Material.EMERALD);
        DROP_MAP.put(Material.DIAMOND_ORE, Material.DIAMOND);
        DROP_MAP.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
        DROP_MAP.put(Material.STONE, Material.COBBLESTONE);

        SMELT_MAP.put(Material.RAW_IRON, Material.IRON_INGOT);
        SMELT_MAP.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.RAW_COPPER, Material.COPPER_INGOT);
    }

    public BlockBreakListener(StorageManager storageManager, List<Material> whitelistItems) {
        this.storageManager = storageManager;
        this.whitelistItems = whitelistItems;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (!DROP_MAP.containsKey(blockType)) return;

        Material dropItem = DROP_MAP.get(blockType);
        if (!whitelistItems.contains(dropItem)) return;

        int amount = getFortuneAmount(player, blockType);

        event.setDropItems(false);
        block.setType(Material.AIR);

        storageManager.addItem(player.getUniqueId(), dropItem, amount);
        player.sendMessage("§a+ " + amount + " " + dropItem.name() + " đã được lưu vào kho!");
    }

    private int getFortuneAmount(Player player, Material blockType) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        int base = 1;
        if (tool != null && tool.containsEnchantment(Enchantment.FORTUNE)) {
            int level = tool.getEnchantmentLevel(Enchantment.FORTUNE);
            return base + new Random().nextInt(level + 2);
        }
        return base;
    }
}
