package com.astralrealms.classes.model.stat;

import net.kyori.adventure.key.Key;

public record StatModifier(Key key, Type type, double value) {

    public enum Type {
        ADDITIVE,
        MULTIPLICATIVE
    }
}
