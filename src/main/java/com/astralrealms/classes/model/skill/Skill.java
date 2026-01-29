package com.astralrealms.classes.model.skill;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.context.SkillContext;

public interface Skill {

    /**
     * Triggers the skill for the given player with an optional context.
     *
     * @param player  The player who is triggering the skill.
     * @param context The context in which the skill is being triggered, can be null.
     */
    void trigger(Player player, InputType inputType, @Nullable SkillContext context);

    double damage();
}
