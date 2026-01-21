package com.astralrealms.classes.configuration;

import java.util.Map;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.stat.StatType;

@ConfigSerializable
public record StatsConfiguration(Map<String, StatType> stats) {
}
