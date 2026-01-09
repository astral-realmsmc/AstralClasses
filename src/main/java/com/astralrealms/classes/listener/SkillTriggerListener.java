package com.astralrealms.classes.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.context.SkillContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillTriggerListener implements Listener {

    private final AstralClasses plugin;

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        this.plugin.skills().tryTriggerSkill(player, InputType.SPACE, () -> SkillContext.ofInput(event.getInput()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null
            || !e.getHand().equals(EquipmentSlot.HAND))
            return;

        this.plugin.skills().tryTriggerSkill(e.getPlayer(), e.getAction().isLeftClick() ? InputType.LEFT_CLICK : InputType.RIGHT_CLICK, () -> null);
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent e) {
        if (e.isSneaking())
            this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SNEAK, () -> null);
    }

    @EventHandler
    public void onSprintToggle(PlayerToggleSneakEvent e) {
        if (e.isSneaking())
            this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SPRINT, () -> null);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.DROP, () -> null);
    }

    @EventHandler
    public void onSwitchHands(PlayerSwapHandItemsEvent e) {
        this.plugin.skills().tryTriggerSkill(e.getPlayer(), InputType.SWAP_HANDS, () -> null);
    }

}
