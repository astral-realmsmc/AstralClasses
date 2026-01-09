package com.astralrealms.classes.model.skill.context;

import org.bukkit.Input;

public interface SkillContext {

    static InputSkillContext ofInput(Input input) {
        return new InputSkillContext(input);
    }
}
