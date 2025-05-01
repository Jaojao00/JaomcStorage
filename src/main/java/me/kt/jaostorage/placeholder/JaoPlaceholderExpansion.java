    package me.kt.jaostorage.placeholder;

    import me.kt.jaostorage.Main;
    import me.kt.jaostorage.storage.StorageManager;
    import me.clip.placeholderapi.expansion.PlaceholderExpansion;
    import org.bukkit.OfflinePlayer;
    import org.bukkit.Material;

    public class JaoPlaceholderExpansion extends PlaceholderExpansion {

        private final Main plugin;

        public JaoPlaceholderExpansion(Main plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getIdentifier() {
            return "jaostorage";
        }

        @Override
        public String getAuthor() {
            return "TaiNguyen";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onRequest(OfflinePlayer player, String identifier) {
            StorageManager storageManager = plugin.getStorageManager();

            // Ví dụ: %jaostorage_diamond%
            try {
                Material material = Material.valueOf(identifier.toUpperCase());
                int amount = storageManager.getStorage(player.getUniqueId()).getOrDefault(material, 0);
                return String.valueOf(amount);
            } catch (IllegalArgumentException e) {
                return "0";
            }
        }
    }
