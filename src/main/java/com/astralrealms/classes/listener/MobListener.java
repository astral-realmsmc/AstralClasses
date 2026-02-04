package com.astralrealms.classes.listener;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.utils.GameUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MobListener implements Listener {

    private final AstralClasses plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHandDamage(EntityDamageByEntityEvent e) {
        // Only handle damage from players
        if (!(e.getDamager() instanceof Player player)
            || e.getDamageSource().getDamageType().equals(DamageType.MAGIC))
            return;

        // Apply damage and effects
        this.plugin.classes()
                .findByPlayer(player)
                .findSkillByInput(InputType.LEFT_CLICK)
                .ifPresentOrElse(skill -> {
                    double damage = GameUtils.computeDamage(player, InputType.LEFT_CLICK, skill.damage());
                    e.setDamage(damage);
                }, () -> e.setCancelled(true));
    }
}
