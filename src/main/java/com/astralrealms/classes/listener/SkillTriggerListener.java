package com.astralrealms.classes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillTriggerListener implements Listener {

    private final AstralClasses plugin;

    @EventHandler
    public void onSwitchHands(PlayerSwapHandItemsEvent e) {
        this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SWAP_HANDS, () -> null);
    }

}
