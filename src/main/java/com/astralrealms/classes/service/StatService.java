package com.astralrealms.classes.service;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.AstralClass;
import com.astralrealms.classes.model.stat.PlayerStats;
import com.astralrealms.classes.model.stat.StatModifier;
import com.astralrealms.classes.model.stat.StatType;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;

@RequiredArgsConstructor
public class StatService {

    private final AstralClasses plugin;
    private final Map<UUID, PlayerStats> playerData = new ConcurrentHashMap<>();

    public void addModifier(Player player, StatModifier modifier) {
        PlayerStats stats = playerData.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats(new HashSet<>()));
        stats.addModifier(modifier);
    }

    public void removeModifier(Player player, StatModifier modifier) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats != null)
            stats.removeModifier(modifier);
    }

    public void removeModifier(Player player, Key key) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats != null)
            stats.removeModifier(key);
    }

    public Double stat(Player player, StatType type) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats == null)
            throw new IllegalStateException("Player stats not found for player: " + player.getName());

        AstralClass astralClass = this.plugin.classes().findByPlayer(player);
        return stats.getStatValue(type, astralClass);
    }

    public Map<StatType, Double> stats(Player player) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats == null)
            throw new IllegalStateException("Player stats not found for player: " + player.getName());

        AstralClass astralClass = this.plugin.classes().findByPlayer(player);
        return stats.applyTo(astralClass.stats());
    }

}
