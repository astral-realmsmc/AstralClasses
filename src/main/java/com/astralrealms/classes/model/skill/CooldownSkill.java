package com.astralrealms.classes.model.skill;

import java.time.Duration;

public interface CooldownSkill extends Skill {

    Duration cooldown();

}
