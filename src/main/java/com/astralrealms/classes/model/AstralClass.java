package com.astralrealms.classes.model;

import java.util.Optional;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.SkillSet;

@ConfigSerializable
public record AstralClass(String id, String name, SkillSet skills) {

    public Optional<Skill> findSkillByInput(InputType inputType) {
        return Optional.ofNullable(this.skills.getSkillByInput(inputType));
    }
}
