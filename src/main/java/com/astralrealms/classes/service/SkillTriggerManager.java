package com.astralrealms.classes.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.astralrealms.classes.AstralClasses;

/**
 * Manages skill trigger throttling to prevent multiple skills from executing
 * in the same server tick for a single player.
 *
 * <p>This prevents simultaneous skill triggers when multiple packets arrive
 * in the same tick (e.g., LEFT_CLICK and RIGHT_CLICK pressed together).</p>
 */
public class SkillTriggerManager {

    private final AstralClasses plugin;
    private final Map<UUID, Long> lastTriggerTick;
    private final int minimumTicksBetweenTriggers;
    private long currentTick;

    /**
     * Creates a new SkillTriggerManager.
     *
     * @param plugin The plugin instance
     * @param minimumTicksBetweenTriggers Minimum ticks required between skill triggers (default: 1)
     */
    public SkillTriggerManager(AstralClasses plugin, int minimumTicksBetweenTriggers) {
        this.plugin = plugin;
        this.lastTriggerTick = new HashMap<>();
        this.minimumTicksBetweenTriggers = minimumTicksBetweenTriggers;
        this.currentTick = 0;
    }

    /**
     * Starts the tick counter task. Should be called during plugin load.
     */
    public void startTickCounter() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            currentTick++;
        }, 1L, 1L);
    }

    /**
     * Checks if a player can trigger a skill at this time.
     *
     * @param player The player to check
     * @return true if the player can trigger a skill, false if they triggered one too recently
     */
    public boolean canTrigger(Player player) {
        UUID uuid = player.getUniqueId();
        Long lastTrigger = lastTriggerTick.get(uuid);

        if (lastTrigger == null) {
            return true; // Never triggered before
        }

        return (currentTick - lastTrigger) >= minimumTicksBetweenTriggers;
    }

    /**
     * Records that a player has triggered a skill at the current tick.
     *
     * @param player The player who triggered the skill
     */
    public void recordTrigger(Player player) {
        lastTriggerTick.put(player.getUniqueId(), currentTick);
    }

    /**
     * Registers a cleanup handler to clear player data when they quit.
     *
     * @param cleanupListener The cleanup listener to register with
     */
    public void clearOnQuit(com.astralrealms.classes.listener.PlayerCleanupListener cleanupListener) {
        cleanupListener.registerHandler(lastTriggerTick::remove);
    }

    /**
     * Gets the current tick count.
     *
     * @return The current tick
     */
    public long getCurrentTick() {
        return currentTick;
    }
}
