package com.astralrealms.classes.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.astralrealms.classes.AstralClasses;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatListener implements Listener {

    private final AstralClasses plugin;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageDealt(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;

        // TODO: Use shield + update armor for display


    }

}
