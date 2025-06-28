package me.kt.jaostorage.storage;

import java.util.*;

public class WhitelistManager {

    // 🔐 Danh sách người chơi được whitelist (theo UUID chủ => set UUID được phép truy cập)
    private final Map<UUID, Set<UUID>> whitelistMap = new HashMap<>();

    // ✅ Thêm người chơi vào danh sách được truy cập kho
    public void addToWhitelist(UUID owner, UUID target) {
        whitelistMap.computeIfAbsent(owner, k -> new HashSet<>()).add(target);
    }

    // ✅ Xoá người chơi khỏi whitelist
    public void removeFromWhitelist(UUID owner, UUID target) {
        Set<UUID> allowed = whitelistMap.get(owner);
        if (allowed != null) {
            allowed.remove(target);
            if (allowed.isEmpty()) {
                whitelistMap.remove(owner);
            }
        }
    }

    // ✅ Kiểm tra xem target có nằm trong whitelist của owner không
    public boolean isWhitelisted(UUID owner, UUID target) {
        return whitelistMap.getOrDefault(owner, Collections.emptySet()).contains(target);
    }

    // ✅ Lấy danh sách tất cả người chơi được owner chia sẻ kho
    public Set<UUID> getWhitelist(UUID owner) {
        return whitelistMap.getOrDefault(owner, Collections.emptySet());
    }

    // ✅ Xoá toàn bộ whitelist của người chơi (nếu reset)
    public void clearWhitelist(UUID owner) {
        whitelistMap.remove(owner);
    }

    // ✅ Kiểm tra xem có ai được chia sẻ không
    public boolean hasShared(UUID owner) {
        return whitelistMap.containsKey(owner) && !whitelistMap.get(owner).isEmpty();
    }
}