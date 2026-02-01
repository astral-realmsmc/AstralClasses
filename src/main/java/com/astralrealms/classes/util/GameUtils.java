package com.astralrealms.classes.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.astralrealms.classes.ClassAPI;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.StatType;

import lombok.experimental.UtilityClass;

/**
 * Consolidated utility for common game operations.
 * Eliminates duplication of damage computation, entity detection, and vector math.
 */
@UtilityClass
public final class GameUtils {

    /**
     * Compute damage for a player with stat modifiers applied.
     *
     * @param player    The player dealing damage
     * @param inputType The input type (LEFT_CLICK, RIGHT_CLICK, etc.)
     * @param baseDamage The base damage value
     * @return The computed damage with all modifiers applied
     */
    public static double computeDamage(Player player, InputType inputType, double baseDamage) {
        return ClassAPI.getStat(player, inputType, StatType.DAMAGE, baseDamage);
    }

    /**
     * Raytrace for living entities in a direction.
     * Used by multiple skills to detect targets.
     *
     * @param start       The starting location
     * @param direction   The direction to raytrace
     * @param maxDistance The maximum distance to raytrace
     * @param hitRadius   The radius around the ray to detect entities
     * @return Collection of hit entities
     */
    public static Collection<LivingEntity> raytraceEntities(Location start, Vector direction, double maxDistance, double hitRadius, double stepSize, Location lastHitLocation) {
        if (!start.isWorldLoaded() || !start.isChunkLoaded() || start.getWorld() == null) {
            return null;
        }

        List<LivingEntity> entities = new ArrayList<>();

        double squaredMaxDistance = maxDistance * maxDistance;
        Location currentLocation = start.clone();
        Vector directionNormalized = direction.clone().normalize();
        int steps = 0;

        do {
            Collection<LivingEntity> nearbyEntities = currentLocation.getNearbyLivingEntities(hitRadius);
            nearbyEntities.forEach(entity -> {
                if (!entities.contains(entity) && !(entity instanceof Player) && entity.hasLineOfSight(start)) {
                    entities.add(entity);
                }
            });
            currentLocation.add(directionNormalized.clone().multiply(stepSize*steps++));
        } while (start.distanceSquared(currentLocation) <= squaredMaxDistance || steps < 1000);

        lastHitLocation.set(currentLocation.x(), currentLocation.y(), currentLocation.z());

        return entities;
    }

    public static LivingEntity raytraceEntity(Location start, Vector direction, double maxDistance, double hitRadius) {
        if (!start.isWorldLoaded() || !start.isChunkLoaded() || start.getWorld() == null) {
            return null;
        }

        RayTraceResult rayTraceResult = start.getWorld().rayTraceEntities(start, direction, maxDistance, hitRadius,
                entity -> entity instanceof LivingEntity && !(entity instanceof Player));
        if (rayTraceResult != null && rayTraceResult.getHitEntity() != null) {
            return (LivingEntity) rayTraceResult.getHitEntity();
        }

        return null;
    }

    /**
     * Get the player's eye location with a small offset.
     * This is the standard location used for skill projectiles.
     *
     * @param player The player
     * @return The eye location with offset
     */
    public static Location getEyeLocation(Player player) {
        return player.getLocation().add(0, player.getEyeHeight() - 0.75, 0);
    }

    /**
     * Rotate a vector around an axis by an angle.
     * Used for helix particle effects.
     *
     * @param vector The vector to rotate
     * @param axis   The axis to rotate around
     * @param angle  The angle in radians
     * @return The rotated vector
     */
    public static Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
        axis = axis.clone().normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = vector.dot(axis);

        return vector.clone()
                .multiply(cos)
                .add(axis.clone().crossProduct(vector).multiply(sin))
                .add(axis.clone().multiply(dot * (1 - cos)));
    }
}
