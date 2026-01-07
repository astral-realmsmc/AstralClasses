package com.astralrealms.classes.skill;

import org.bukkit.Input;
import org.bukkit.entity.Player;

public interface Skill {

    void trigger(Player player, Input input);

}
