package me.kt.jaostorage.storage;

import me.kt.jaostorage.Main;
import me.kt.jaostorage.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Quản lý hệ thống chia sẻ kho (Coop).
 * Owner có thể thêm member để cùng truy cập kho.
 * Dữ liệu persist trong SQLite.
 */
public class CoopManager {

    private final Main plugin;
    private final DatabaseManager db;
    private final Map<UUID, Set<UUID>> coopCache = new ConcurrentHashMap<>();

    public CoopManager(Main plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        loadAll();
    }

    private void loadAll() {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT owner_uuid, member_uuid FROM coop_members");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner_uuid"));
                UUID member = UUID.fromString(rs.getString("member_uuid"));
                coopCache.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(member);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Không thể tải dữ liệu Coop!", e);
        }
    }

    /**
     * Thêm member vào coop của owner.
     */
    public boolean addCoop(UUID ownerUUID, UUID memberUUID) {
        if (ownerUUID.equals(memberUUID)) return false;
        if (isCoop(ownerUUID, memberUUID)) return false;

        coopCache.computeIfAbsent(ownerUUID, k -> ConcurrentHashMap.newKeySet()).add(memberUUID);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "INSERT OR IGNORE INTO coop_members (owner_uuid, member_uuid) VALUES (?, ?)")) {
                ps.setString(1, ownerUUID.toString());
                ps.setString(2, memberUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "❌ Lỗi thêm Coop member!", e);
            }
        });
        return true;
    }

    public void addCoop(Player owner, Player member) {
        addCoop(owner.getUniqueId(), member.getUniqueId());
    }

    /**
     * Xóa member khỏi coop.
     */
    public boolean removeCoop(UUID ownerUUID, UUID memberUUID) {
        Set<UUID> members = coopCache.get(ownerUUID);
        if (members == null || !members.remove(memberUUID)) return false;

        if (members.isEmpty()) {
            coopCache.remove(ownerUUID);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "DELETE FROM coop_members WHERE owner_uuid = ? AND member_uuid = ?")) {
                ps.setString(1, ownerUUID.toString());
                ps.setString(2, memberUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "❌ Lỗi xóa Coop member!", e);
            }
        });
        return true;
    }

    public void removeCoop(Player owner, Player member) {
        removeCoop(owner.getUniqueId(), member.getUniqueId());
    }

    /**
     * Kiểm tra xem member có trong coop của owner không.
     */
    public boolean isCoop(UUID ownerUUID, UUID memberUUID) {
        Set<UUID> members = coopCache.get(ownerUUID);
        return members != null && members.contains(memberUUID);
    }

    public boolean isCoop(Player owner, Player member) {
        return isCoop(owner.getUniqueId(), member.getUniqueId());
    }

    /**
     * Kiểm tra xem viewer có quyền truy cập kho của target không.
     * (là chính mình HOẶC là coop member HOẶC là admin)
     */
    public boolean hasAccess(UUID viewerUUID, UUID targetUUID) {
        return viewerUUID.equals(targetUUID) || isCoop(targetUUID, viewerUUID);
    }

    /**
     * Lấy danh sách member của owner.
     */
    public Set<UUID> getMembers(UUID ownerUUID) {
        return coopCache.getOrDefault(ownerUUID, Collections.emptySet());
    }

    /**
     * Lấy danh sách kho mà player được chia sẻ (player là member).
     */
    public Set<UUID> getSharedWith(UUID memberUUID) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, Set<UUID>> entry : coopCache.entrySet()) {
            if (entry.getValue().contains(memberUUID)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Xóa toàn bộ coop của owner.
     */
    public void clearCoop(UUID ownerUUID) {
        coopCache.remove(ownerUUID);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "DELETE FROM coop_members WHERE owner_uuid = ?")) {
                ps.setString(1, ownerUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "❌ Lỗi xóa toàn bộ Coop!", e);
            }
        });
    }

    public int getMemberCount(UUID ownerUUID) {
        return getMembers(ownerUUID).size();
    }
}
