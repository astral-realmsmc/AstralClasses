package com.astralrealms.classes.configuration;

import java.util.Map;

import javax.annotation.Nullable;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.StatType;

@ConfigSerializable
public record StatsConfiguration(Map<InputType, Map<StatType, String>> expressions) {

    public @Nullable String getExpression(InputType inputType, StatType statType) {
        Map<StatType, String> statMap = expressions.get(inputType);
        if (statMap == null) return null;
        return statMap.get(statType);
    }

}
