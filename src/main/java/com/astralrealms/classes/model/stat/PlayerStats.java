package com.astralrealms.classes.model.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.astralrealms.classes.model.AstralClass;

import net.kyori.adventure.key.Key;

public record PlayerStats(Set<StatModifier> modifiers) {

    public void addModifier(StatModifier modifier) {
        modifiers.add(modifier);
    }

    public void removeModifier(StatModifier modifier) {
        modifiers.remove(modifier);
    }

    public void removeModifier(Key key) {
        modifiers.removeIf(modifier -> modifier.key().equals(key));
    }

    public double getStatValue(StatType type, AstralClass astralClass) {
        return getStatValue(type, astralClass.getBaseStatValue(type));
    }

    public double getStatValue(StatType type, double baseValue) {
        double modifiedValue = baseValue;
        for (StatModifier modifier : modifiers) {
            if (modifier.type() != type)
                continue;

            switch (modifier.operation()) {
                case ADDITIVE -> modifiedValue += modifier.value();
                case MULTIPLICATIVE -> modifiedValue *= modifier.value();
            }
        }
        return modifiedValue;
    }

    public Map<StatType, Double> applyTo(Map<StatType, Double> baseStats) {
        Map<StatType, Double> modifiedStats = new HashMap<>(baseStats);

        for (StatModifier modifier : modifiers) {
            StatType type = modifier.type();
            StatModifier.Type operation = modifier.operation();

            modifiedStats.putIfAbsent(type, 0.0);
            double currentValue = modifiedStats.get(type);
            switch (operation) {
                case ADDITIVE -> modifiedStats.put(type, currentValue + modifier.value());
                case MULTIPLICATIVE -> modifiedStats.put(type, currentValue * modifier.value());
            }
        }

        return modifiedStats;
    }
}
