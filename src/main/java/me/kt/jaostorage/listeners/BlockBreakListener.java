package me.kt.jaostorage.listeners;

import me.kt.jaostorage.storage.StorageManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;

import java.util.*;

public class BlockBreakListener implements Listener {

    private final StorageManager storageManager;
    private final List<Material> whitelistItems; // ✅ Danh sách item được lưu kho

    // Map từ block quặng → vật phẩm drop
    private static final Map<Material, Material> DROP_MAP = new HashMap<>();

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
        DROP_MAP.put(Material.STONE, Material.COBBLESTONE); // 🎯 thêm nếu muốn đá cũng vào kho
    }

    public BlockBreakListener(StorageManager storageManager, List<Material> whitelistItems) {
        this.storageManager = storageManager;
        this.whitelistItems = whitelistItems;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID uuid = player.getUniqueId();
        Material blockType = block.getType();

        // ❗ Nếu block không nằm trong DROP_MAP, bỏ qua
        if (!DROP_MAP.containsKey(blockType)) return;

        // Xác định vật phẩm thực tế sẽ nhận được khi khai thác
        Material dropItem = DROP_MAP.get(blockType);

        // ❌ Nếu vật phẩm không nằm trong danh sách whitelist → không đưa vào kho
        if (!whitelistItems.contains(dropItem)) return;

        // ✅ Hủy drop ra ngoài
        event.setDropItems(false);

        // ✅ Tính lượng item dựa theo Fortune
        int amount = getFortuneAmount(player, blockType);

        // ✅ Thêm vào kho
        storageManager.addItem(player, dropItem, amount);
    }

    private int getFortuneAmount(Player player, Material blockType) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        int base = 1;

        if (tool != null && tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            int level = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            return base + new Random().nextInt(level + 2); // Fortune basic logic
        }

        return base;
    }
}
