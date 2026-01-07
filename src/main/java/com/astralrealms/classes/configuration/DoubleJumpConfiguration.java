package com.astralrealms.classes.configuration;

import java.time.Duration;

import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record DoubleJumpConfiguration(Vector verticalVelocityMultiplier,
                                      Vector horizontalVelocityMultiplier,
                                      int maxConsecutiveJumps,
                                      Duration cooldown) {
}
