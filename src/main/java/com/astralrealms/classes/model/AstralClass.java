package com.astralrealms.classes.model;

import java.util.Map;
import java.util.Optional;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.SkillSet;
import com.astralrealms.classes.model.stat.StatType;

@ConfigSerializable
public record AstralClass(String id, String name, SkillSet skills, Map<StatType, Double> stats) {

    public Optional<Skill> findSkillByInput(InputType inputType) {
        return Optional.ofNullable(this.skills.getSkillByInput(inputType));
    }

    public double getBaseStatValue(StatType type) {
        return this.stats.getOrDefault(type, 1.0);
    }
}
