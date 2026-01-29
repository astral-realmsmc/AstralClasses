package com.astralrealms.classes.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.AstralClass;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.PlayerStats;
import com.astralrealms.classes.model.stat.StatModifier;
import com.astralrealms.classes.model.stat.StatType;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attribute;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import net.kyori.adventure.key.Key;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;

public class StatService {

    private final AstralClasses plugin;
    private final Map<UUID, PlayerStats> playerData = new ConcurrentHashMap<>();
    private final Set<UUID> needsRegeneration = ConcurrentHashMap.newKeySet();

    public StatService(AstralClasses plugin) {
        this.plugin = plugin;

        // Regeneration task - lazy, only processes damaged players
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Map<StatType, Double> regenPerSecond = this.plugin.statsConfiguration().regenerationPerSecond();
            long currentTime = System.currentTimeMillis();

            // Only process players who need regeneration
            for (UUID uuid : needsRegeneration) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    needsRegeneration.remove(uuid);
                    continue;
                }

                PlayerStats stats = playerData.get(uuid);
                if (stats == null) {
                    needsRegeneration.remove(uuid);
                    continue;
                }

                boolean fullyHealed = true;
                Map<StatType, Long> lastRegenTimes = stats.lastRegenTimes();

                for (Map.Entry<StatType, Double> entry : regenPerSecond.entrySet()) {
                    StatType statType = entry.getKey();
                    double regenAmount = entry.getValue();

                    long lastRegenTime = lastRegenTimes.getOrDefault(statType, 0L);
                    if (currentTime - lastRegenTime >= 1000L) {
                        double maxStatValue = stats.computedStats().get(statType);
                        double currentValue = stats.getStatValue(statType);
                        if (currentValue < maxStatValue) {
                            stats.setStatValue(statType, currentValue + regenAmount);
                            lastRegenTimes.put(statType, currentTime);
                            fullyHealed = false;
                        }
                    }
                }

                // Remove from set if fully healed
                if (fullyHealed) {
                    needsRegeneration.remove(uuid);
                }
            }
        }, 100L, 20L);

        // Shield display
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats stats = playerData.get(player.getUniqueId());
                if (stats == null)
                    continue;

                double shieldValue = stats.getStatValue(StatType.SHIELD);
                double maximumShieldValue = stats.computedStats().get(StatType.SHIELD);
                if (maximumShieldValue <= 0)
                    continue;

                double value = shieldValue / maximumShieldValue;

                Attribute attribute = Attributes.ARMOR;
                WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(
                        player.getEntityId(),
                        List.of(
                                new WrapperPlayServerUpdateAttributes.Property(
                                        attribute,
                                        value * 20,
                                        Collections.emptyList()
                                )
                        )
                );
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }, 100L, 5L);
    }

    /**
     * Recompute and update cached stats for a player.
     * Extracted to eliminate duplication in initStats, addModifier, removeModifier.
     *
     * @param player The player to recompute stats for
     */
    private void recomputeStats(Player player) {
        Map<StatType, Double> computedStats = computeGlobalStats(player);
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats != null) {
            stats.computedStats().putAll(computedStats);
        }
    }

    public void initStats(Player player) {
        PlayerStats stats = new PlayerStats(new ArrayList<>(), new HashMap<>(), new HashMap<>(), new ConcurrentHashMap<>(), new HashMap<>());
        this.playerData.put(player.getUniqueId(), stats);

        // Compute and cache global stats
        recomputeStats(player);

        // Add variable stats
        Map<StatType, Double> computedStats = stats.computedStats();
        for (Map.Entry<StatType, Double> entry : computedStats.entrySet()) {
            if (!entry.getKey().variable())
                continue;

            stats.setStatValue(entry.getKey(), entry.getValue());
        }

        // Add to regeneration set
        markNeedsRegeneration(player);
    }

    /**
     * Mark a player as needing regeneration.
     * Called when a player takes damage.
     *
     * @param player The player to mark
     */
    public void markNeedsRegeneration(Player player) {
        needsRegeneration.add(player.getUniqueId());
    }

    public void addModifier(Player player, StatModifier modifier) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats == null)
            return;

        stats.addGlobalModifier(modifier);
        recomputeStats(player);
    }

    public void removeModifier(Player player, StatModifier modifier) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats == null)
            return;

        stats.removeModifier(modifier);
        recomputeStats(player);
    }

    public void removeModifier(Player player, Key key) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats == null)
            return;

        stats.removeModifier(key);
        recomputeStats(player);
    }

    public double computeStat(Player player, InputType inputType, StatType statType, double value) {
        AstralClass astralClass = this.plugin.classes().findByPlayer(player);
        PlayerStats stats = playerData.get(player.getUniqueId());

        double baseValue = astralClass.getBaseStatValue(statType) + value;

        // Compute global modifiers
        double globalBonusValue = this.applyModifiers(
                1.0,
                stats != null ? new ArrayList<>(stats.globalModifiers()) : Collections.emptyList()
        );

        // Compute input modifiers
        double inputBonusValue = 1.0;
        if (stats != null) {
            List<StatModifier> inputModifiers = stats.inputModifiers().get(inputType);
            if (inputModifiers != null) {
                inputBonusValue = this.applyModifiers(1.0, inputModifiers);
            }
        }

        // Compute formula if exists
        String expression = this.plugin.statsConfiguration().getExpression(inputType, statType);
        if (expression != null && !expression.isEmpty()) {
            EvaluationEnvironment env = new EvaluationEnvironment();
            env.setVariableNames(
                    "baseValue",
                    "globalBonusValue",
                    "inputBonusValue",
                    "classCriticalDamage",
                    "classCriticalChance"
            );
            CompiledExpression compiledExpression = Crunch.compileExpression(expression, env);
            return compiledExpression.evaluate(
                    baseValue,
                    globalBonusValue,
                    inputBonusValue,
                    astralClass.getBaseStatValue(StatType.CRITICAL_DAMAGE),
                    astralClass.getBaseStatValue(StatType.CRITICAL_CHANCE)
            );
        }

        return baseValue;
    }

    public Map<StatType, Double> computeInputStats(Player player, InputType inputType, StatType statType) {
        Map<StatType, Double> computedStats = new HashMap<>();
        PlayerStats stats = playerData.get(player.getUniqueId());

        List<StatModifier> modifiers = stats.inputModifiers().get(inputType);
        if (modifiers != null) {
            for (StatModifier modifier : modifiers) {
                if (modifier.type() != statType) continue;

                StatModifier.StatOperation operation = modifier.operation();

                double currentValue = computedStats.getOrDefault(statType, 1.0);
                double modifierValue = modifier.value();
                currentValue = operation.apply(currentValue, modifierValue);
                computedStats.put(statType, currentValue);
            }
        }
        return computedStats;
    }

    public Map<StatType, Double> computeGlobalStats(Player player) {
        AstralClass astralClass = this.plugin.classes().findByPlayer(player);

        Map<StatType, Double> computedStats = new HashMap<>(astralClass.stats());
        PlayerStats stats = playerData.get(player.getUniqueId());

        for (StatModifier modifier : stats.globalModifiers()) {
            StatModifier.StatOperation operation = modifier.operation();
            StatType statType = modifier.type();

            double currentValue = computedStats.getOrDefault(statType, 1.0);
            double modifierValue = modifier.value();
            currentValue = operation.apply(currentValue, modifierValue);

            computedStats.put(statType, currentValue);
        }

        return computedStats;
    }

    private double applyModifiers(double value, List<StatModifier> modifiers) {
        double modifiedValue = value;
        for (StatModifier modifier : modifiers) {
            StatModifier.StatOperation operation = modifier.operation();
            double modifierValue = modifier.value();
            modifiedValue = operation.apply(modifiedValue, modifierValue);
        }
        return modifiedValue;
    }

    public Optional<PlayerStats> getPlayerStats(Player player) {
        return Optional.ofNullable(playerData.get(player.getUniqueId()));
    }
}
