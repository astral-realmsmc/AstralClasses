package com.astralrealms.classes.model;

import org.bukkit.Input;

public enum InputType {
    SPACE,
    SNEAK,
    SPRINT,
    LEFT_CLICK,
    RIGHT_CLICK,
    DROP,
    SWAP_HANDS;

    public static InputType fromInput(Input input) {
        if (input.isSneak())
            return SNEAK;
        if (input.isSprint())
            return SPRINT;
        if (input.isJump())
            return SPACE;
        if (input.isSneak())
            return SNEAK;
        return null;
    }
}
