package com.astralrealms.classes.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Input;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.state.JumpState;
import com.astralrealms.classes.skill.BasicShootSkill;
import com.astralrealms.classes.skill.DoubleJumpSkill;

/**
 * Handles double jump skill detection and triggering.
 * Uses jump-count based detection for reliable triggering.
 */
public class SkillListener implements Listener {

    private final AstralClasses plugin;
    private final DoubleJumpSkill doubleJumpSkill;
    private final Map<UUID, JumpState> jumpStates = new ConcurrentHashMap<>();

    public SkillListener(AstralClasses plugin) {
        this.plugin = plugin;
        this.doubleJumpSkill = new DoubleJumpSkill();
    }

    /**
     * Tracks when players touch the ground to reset their jump count.
     * Only processes when player moves between blocks for performance.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Early exit: only process when moving between blocks
        if (!event.hasChangedBlock())
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Reset jump state when player touches ground
        if (!isOnGround(player))
            return;

        JumpState state = jumpStates.get(uuid);
        if (state != null && state.jumpCount() > 0)
            state.reset();
    }

    /**
     * Handles jump input and triggers double jump skill.
     * First jump: Record that player jumped
     * Second jump (while airborne): Trigger double jump skill
     */
    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        Input input = event.getInput();
        if (!input.isJump())
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Get or create jump state for this player
        JumpState state = jumpStates.computeIfAbsent(uuid, _ -> new JumpState());

        // Player on ground = first jump
        if (isOnGround(player)) {
            state.reset();
            state.recordJump();
            return;
        }

        // Player airborne = potential double jump
        long currentTime = System.currentTimeMillis();
        long cooldownMs = plugin.doubleJumpConfiguration().cooldown().toMillis();
        int maxJumps = plugin.doubleJumpConfiguration().maxConsecutiveJumps();

        if (!state.canDoubleJump(currentTime, cooldownMs, maxJumps)) {
            return;
        }

        // Trigger double jump
        state.recordDoubleJump(currentTime);
        doubleJumpSkill.trigger(player, input);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        BasicShootSkill skill = new BasicShootSkill();
        skill.trigger(e.getPlayer(), new Input() {
            @Override
            public boolean isForward() {
                return false;
            }

            @Override
            public boolean isBackward() {
                return false;
            }

            @Override
            public boolean isLeft() {
                return false;
            }

            @Override
            public boolean isRight() {
                return false;
            }

            @Override
            public boolean isJump() {
                return false;
            }

            @Override
            public boolean isSneak() {
                return false;
            }

            @Override
            public boolean isSprint() {
                return false;
            }
        });
    }

    /**
     * Clean up jump states when players leave.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        jumpStates.remove(event.getPlayer().getUniqueId());
    }

    private boolean isOnGround(Player player) {
        Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
        return block.getType().isSolid();
    }
}
