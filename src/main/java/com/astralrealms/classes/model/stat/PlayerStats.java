package com.astralrealms.classes.model.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.astralrealms.classes.model.InputType;

import net.kyori.adventure.key.Key;

public record PlayerStats(List<StatModifier> globalModifiers, Map<InputType, List<StatModifier>> inputModifiers) {

    public void addGlobalModifier(StatModifier modifier) {
        globalModifiers.add(modifier);
    }

    public void addInputModifier(InputType inputType, StatModifier modifier) {
        inputModifiers.computeIfAbsent(inputType, _ -> new ArrayList<>()).add(modifier);
    }

    public void removeModifier(StatModifier modifier) {
        globalModifiers.remove(modifier);
        inputModifiers.values().forEach(modifiers -> modifiers.remove(modifier));
    }

    public void removeModifier(Key key) {
        globalModifiers.removeIf(modifier -> modifier.key().equals(key));
        inputModifiers.values().forEach(modifiers -> modifiers.removeIf(modifier -> modifier.key().equals(key)));
    }

    public List<StatModifier> inputModifiers(InputType inputType) {
        return inputModifiers.getOrDefault(inputType, List.of());
    }

    public List<StatModifier> inputModifiers(InputType inputType, StatType statType) {
        return inputModifiers(inputType)
                .stream()
                .filter(modifier -> modifier.type().equals(statType))
                .toList();
    }

    public List<StatModifier> globalModifiers(StatType statType) {
        return globalModifiers
                .stream()
                .filter(modifier -> modifier.type().equals(statType))
                .toList();
    }
}
