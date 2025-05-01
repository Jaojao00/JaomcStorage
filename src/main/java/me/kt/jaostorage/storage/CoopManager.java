package me.kt.jaostorage.storage;

import org.bukkit.entity.Player;

import java.util.*;

public class CoopManager {

    private final Map<UUID, Set<UUID>> coopMap = new HashMap<>();

    public void addCoop(Player owner, Player member) {
        coopMap.computeIfAbsent(owner.getUniqueId(), k -> new HashSet<>()).add(member.getUniqueId());
    }

    public void removeCoop(Player owner, Player member) {
        Set<UUID> members = coopMap.get(owner.getUniqueId());
        if (members != null) {
            members.remove(member.getUniqueId());
            if (members.isEmpty()) {
                coopMap.remove(owner.getUniqueId());
            }
        }
    }

    public boolean isCoop(Player owner, Player member) {
        Set<UUID> members = coopMap.get(owner.getUniqueId());
        return members != null && members.contains(member.getUniqueId());
    }

    public Set<UUID> getCoops(Player owner) {
        return coopMap.getOrDefault(owner.getUniqueId(), new HashSet<>());
    }
}
