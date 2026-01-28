package com.astralrealms.classes.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.core.model.wrapper.ComponentWrapper;

@ConfigSerializable
public record MainConfiguration(String defaultClass, DamageIndicators damageIndicators) {

    @ConfigSerializable
    public record DamageIndicators(ComponentWrapper immune, ComponentWrapper damaged) {
    }
}
