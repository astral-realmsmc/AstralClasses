package com.astralrealms.classes.configuration;

import java.util.Map;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.InputType;

@ConfigSerializable
public record StatsConfiguration(Map<InputType, String> damageExpressions) {
}
