package com.astralrealms.classes.model.stat;

import net.kyori.adventure.key.Key;

public record StatModifier(Key key, StatType type, StatOperation operation, double value, boolean transientModifier) {

    /**
     * Backward-compatible factory method for creating modifiers with enum operations.
     *
     * @param key      The modifier key
     * @param type     The stat type
     * @param operation The operation enum
     * @param value    The modifier value
     * @return A new StatModifier
     */
    public static StatModifier of(Key key, StatType type, Operation operation, double value) {
        return new StatModifier(key, type, operation, value, false);
    }

    /**
     * Built-in stat operations.
     */
    public enum Operation implements StatOperation {
        ADDITIVE {
            @Override
            public double apply(double base, double modifier) {
                return base + modifier;
            }
        },
        MULTIPLICATIVE {
            @Override
            public double apply(double base, double modifier) {
                return base * modifier;
            }
        };
    }

    /**
     * Interface for stat modifier operations.
     * Allows custom operations beyond the built-in enum.
     */
    public interface StatOperation {
        /**
         * Apply this operation to a base value.
         *
         * @param base     The base value
         * @param modifier The modifier value
         * @return The result of applying the operation
         */
        double apply(double base, double modifier);
    }
}
