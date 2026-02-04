package com.astralrealms.classes.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record MainConfiguration(String defaultClass) {

}
