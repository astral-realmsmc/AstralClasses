package com.astralrealms.classes;

import org.bukkit.entity.Player;

import com.astralrealms.classes.model.AstralClass;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.StatType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassAPI {

    private static AstralClasses plugin;

    static void init(AstralClasses astralClasses) {
        plugin = astralClasses;
    }

    public static double getStat(Player player, InputType inputType, StatType type, double value) {
        return plugin.stats().computeStat(player, inputType, type, value);
    }

    public static AstralClass selectedClass(Player player) {
        return plugin.classes().findByPlayer(player);
    }
}
