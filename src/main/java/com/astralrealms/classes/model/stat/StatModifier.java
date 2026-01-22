package com.astralrealms.classes.model.stat;

import net.kyori.adventure.key.Key;

public record StatModifier(Key key, StatType type, Type operation, double value, boolean transientModifier) {

    public enum Type {
        ADDITIVE,
        MULTIPLICATIVE
    }
}
