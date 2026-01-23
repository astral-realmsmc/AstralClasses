package com.astralrealms.classes.model.stat;

import net.kyori.adventure.key.Key;

public record StatModifier(Key key, StatType type, Operation operation, double value, boolean transientModifier) {

    public enum Operation {
        ADDITIVE,
        MULTIPLICATIVE
    }
}
