package com.astralrealms.classes.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.AstralClass;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.PlayerStats;
import com.astralrealms.classes.model.stat.StatModifier;
import com.astralrealms.classes.model.stat.StatType;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;

@RequiredArgsConstructor
public class StatService {

    private final AstralClasses plugin;
    private final Map<UUID, PlayerStats> playerData = new ConcurrentHashMap<>();

    public void addModifier(Player player, StatModifier modifier) {
        PlayerStats stats = playerData.computeIfAbsent(player.getUniqueId(), _ -> new PlayerStats(new ArrayList<>(), new HashMap<>()));
        stats.addGlobalModifier(modifier);
    }

    public void removeModifier(Player player, StatModifier modifier) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats != null)
            stats.removeModifier(modifier);
    }

    public void removeModifier(Player player, Key key) {
        PlayerStats stats = playerData.get(player.getUniqueId());
        if (stats != null)
            stats.removeModifier(key);
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

                StatModifier.Operation operation = modifier.operation();

                double currentValue = computedStats.getOrDefault(statType, 1.0);
                double modifierValue = modifier.value();
                switch (operation) {
                    case ADDITIVE -> currentValue += modifierValue;
                    case MULTIPLICATIVE -> currentValue *= modifierValue;
                }
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
            StatModifier.Operation operation = modifier.operation();
            StatType statType = modifier.type();

            double currentValue = computedStats.getOrDefault(statType, 1.0);
            double modifierValue = modifier.value();
            switch (operation) {
                case ADDITIVE -> currentValue += modifierValue;
                case MULTIPLICATIVE -> currentValue *= modifierValue;
            }

            computedStats.put(statType, currentValue);
        }

        return computedStats;
    }

    private double applyModifiers(double value, List<StatModifier> modifiers) {
        double modifiedValue = value;
        for (StatModifier modifier : modifiers) {
            StatModifier.Operation operation = modifier.operation();
            double modifierValue = modifier.value();
            switch (operation) {
                case ADDITIVE -> modifiedValue += modifierValue;
                case MULTIPLICATIVE -> modifiedValue *= modifierValue;
            }
        }
        return modifiedValue;
    }
}
