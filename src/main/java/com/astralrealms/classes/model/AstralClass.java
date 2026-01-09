package com.astralrealms.classes.model;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.skill.SkillSet;

@ConfigSerializable
public record AstralClass(String id, String name, SkillSet skills) {
}
