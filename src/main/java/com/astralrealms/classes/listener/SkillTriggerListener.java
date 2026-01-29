package com.astralrealms.classes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillTriggerListener implements Listener {

    private final AstralClasses plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null
            || !e.getHand().equals(EquipmentSlot.HAND)
            || e.getAction().isRightClick())
            return;

        this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.LEFT_CLICK, () -> null);
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent e) {
        if (e.isSneaking())
            this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SNEAK, () -> null);
    }

    @EventHandler
    public void onSprintToggle(PlayerToggleSprintEvent e) {
        if (e.isSprinting())
            this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SPRINT, () -> null);
    }

    @EventHandler
    public void onSwitchHands(PlayerSwapHandItemsEvent e) {
        this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SWAP_HANDS, () -> null);
    }

}
