package com.astralrealms.classes.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.astralrealms.classes.listener.PlayerCleanupListener;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.stats.StatsAPI;
import com.astralrealms.stats.model.stat.StatType;

/**
 * Manages skill cooldowns with attack speed modification.
 * Extracted from SkillService to separate concerns.
 */
public class CooldownManager {

    private final Map<UUID, Map<Class<? extends Skill>, Long>> timestamps = new HashMap<>();

    /**
     * Check if a player can use a cooldown skill.
     *
     * @param player The player attempting to use the skill
     * @param skill  The cooldown skill to check
     * @return true if the skill can be used (not on cooldown)
     */
    public boolean canUse(Player player, CooldownSkill skill, InputType inputType) {
        long currentTime = System.currentTimeMillis();
        Map<Class<? extends Skill>, Long> playerTimestamps = this.timestamps.computeIfAbsent(player.getUniqueId(), _ -> new HashMap<>());
        long lastUsed = playerTimestamps.getOrDefault(skill.getClass(), 0L);

        // Get the modified cooldown based on ATTACK_SPEED
        long baseCooldownMillis = skill.cooldown().toMillis();
        double attackSpeed = StatsAPI.stat(player, StatType.ATTACK_SPEED.key(), inputType);
        long modifiedCooldownMillis = (long) (baseCooldownMillis / attackSpeed);

        return currentTime - lastUsed >= modifiedCooldownMillis;
    }

    /**
     * Record a skill usage for cooldown tracking.
     *
     * @param player The player using the skill
     * @param skill  The skill being used
     */
    public void recordUsage(Player player, Skill skill) {
        Map<Class<? extends Skill>, Long> playerTimestamps = this.timestamps.computeIfAbsent(player.getUniqueId(), _ -> new HashMap<>());
        playerTimestamps.put(skill.getClass(), System.currentTimeMillis());
    }

    /**
     * Get the remaining cooldown time in milliseconds for a skill.
     *
     * @param player The player to check
     * @param skill  The cooldown skill
     * @return The remaining cooldown in milliseconds, or 0 if not on cooldown
     */
    public long getRemainingCooldown(Player player, CooldownSkill skill) {
        long currentTime = System.currentTimeMillis();
        Map<Class<? extends Skill>, Long> playerTimestamps = this.timestamps.get(player.getUniqueId());
        if (playerTimestamps == null)
            return 0L;

        Long lastUsed = playerTimestamps.get(skill.getClass());
        if (lastUsed == null)
            return 0L;

        // Get the modified cooldown based on ATTACK_SPEED
        long baseCooldownMillis = skill.cooldown().toMillis();
        double attackSpeed = StatsAPI.stat(player, StatType.ATTACK_SPEED);
        long modifiedCooldownMillis = (long) (baseCooldownMillis / attackSpeed);

        long elapsed = currentTime - lastUsed;
        return Math.max(0, modifiedCooldownMillis - elapsed);
    }

    /**
     * Clear cooldown data for a player (called on quit).
     *
     * @param listener The cleanup listener to register with
     */
    public void clearOnQuit(PlayerCleanupListener listener) {
        listener.registerHandler(timestamps::remove);
    }

    /**
     * Clear all cooldown data.
     */
    public void clear() {
        timestamps.clear();
    }
}
