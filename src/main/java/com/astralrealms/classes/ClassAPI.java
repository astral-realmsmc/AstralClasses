package com.astralrealms.classes;

import org.bukkit.entity.Player;

import com.astralrealms.classes.model.AstralClass;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassAPI {

    private static AstralClasses plugin;

    static void init(AstralClasses astralClasses) {
        plugin = astralClasses;
    }

    public static AstralClass selectedClass(Player player) {
        return plugin.classes().findByPlayer(player);
    }
}
