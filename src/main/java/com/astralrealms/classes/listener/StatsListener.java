package com.astralrealms.classes.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.stat.PlayerStats;
import com.astralrealms.classes.model.stat.StatType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatsListener implements Listener {

    private final AstralClasses plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.plugin.stats().initStats(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShieldUse(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;

        double finalDamage = e.getFinalDamage();
        PlayerStats stats = this.plugin.stats()
                .getPlayerStats(player)
                .orElse(null);
        if (stats == null)
            return;

        // Mark player as needing regeneration when they take damage
        if (finalDamage > 0) {
            this.plugin.stats().markNeedsRegeneration(player);
        }

        double shield = stats.getStatValue(StatType.SHIELD);
        if (shield <= 0)
            return;

        if (finalDamage >= shield) {
            e.setDamage(finalDamage - shield);
            stats.setStatValue(StatType.SHIELD, 0);
        } else {
            e.setDamage(0);
            stats.setStatValue(StatType.SHIELD, shield - finalDamage);
        }
    }
}
