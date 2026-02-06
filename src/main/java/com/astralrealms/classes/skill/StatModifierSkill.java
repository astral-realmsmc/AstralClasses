package com.astralrealms.classes.skill;

import java.time.Duration;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;

public record StatModifierSkill() implements CooldownSkill, Tickable {

    @Override
    public void tick() {

    }

    @Override
    public void trigger(Player player, InputType inputType, @Nullable SkillContext context) {

    }


    @Override
    public Duration cooldown() {
        return null;
    }
}
