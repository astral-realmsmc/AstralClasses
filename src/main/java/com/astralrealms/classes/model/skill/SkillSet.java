package com.astralrealms.classes.model.skill;

import java.util.Map;

import com.astralrealms.classes.model.InputType;

public record SkillSet(Map<InputType, Skill> skills) {
}
