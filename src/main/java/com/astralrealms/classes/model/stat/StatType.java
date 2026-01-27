package com.astralrealms.classes.model.stat;

import lombok.Getter;

@Getter
public enum StatType {
    DAMAGE,
    CRITICAL_DAMAGE,
    CRITICAL_CHANCE,
    ATTACK_SPEED,
    SHIELD(true),
    SPEED;

    private final boolean variable;

    StatType() {
        this.variable = false;
    }

    StatType(boolean variable) {
        this.variable = variable;
    }

    public boolean percentageBased() {
        return this == CRITICAL_CHANCE || this == ATTACK_SPEED || this == SPEED;
    }

}
