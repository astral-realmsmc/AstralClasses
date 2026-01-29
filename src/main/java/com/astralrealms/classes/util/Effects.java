package com.astralrealms.classes.util;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

import com.destroystokyo.paper.ParticleBuilder;

import lombok.experimental.UtilityClass;

/**
 * Consolidated utility for sound and particle effects.
 * Eliminates duplication of particle/sound spawning code across skills.
 */
@UtilityClass
public final class Effects {

    // Cached particle builders for common particle types to avoid repeated construction
    private static final Map<Particle, ParticleBuilder> CACHED_BUILDERS = new EnumMap<>(Particle.class);

    static {
        // Pre-create commonly used particle builders
        CACHED_BUILDERS.put(Particle.SMOKE, Particle.SMOKE.builder().offset(0, 0, 0).count(1).extra(0f));
        CACHED_BUILDERS.put(Particle.FLAME, Particle.FLAME.builder().offset(0, 0, 0).count(1).extra(0f));
        CACHED_BUILDERS.put(Particle.CLOUD, Particle.CLOUD.builder().count(10).offset(0.5, 0.2, 0.5).extra(0));
        CACHED_BUILDERS.put(Particle.EXPLOSION, Particle.EXPLOSION.builder().count(5).offset(0.5, 0.5, 0.5).extra(0));
        CACHED_BUILDERS.put(Particle.LARGE_SMOKE, Particle.LARGE_SMOKE.builder().count(5).offset(0.2, 0.2, 0.2).extra(0));
    }

    /**
     * Play a sound at a location.
     *
     * @param location The location to play the sound at
     * @param sound    The sound to play
     * @param volume   The volume of the sound
     * @param pitch    The pitch of the sound
     */
    public static void playSound(Location location, Sound sound, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    /**
     * Spawn particles at a location using cached builder when available.
     *
     * @param location The location to spawn particles at
     * @param type     The particle type
     * @param count    The number of particles
     * @param offsetX  The X offset
     * @param offsetY  The Y offset
     * @param offsetZ  The Z offset
     * @param extra    The extra data
     */
    public static void spawnParticle(Location location, Particle type, int count, double offsetX, double offsetY,
                                     double offsetZ, double extra) {
        ParticleBuilder builder = CACHED_BUILDERS.get(type);
        if (builder != null) {
            // Use cached builder, override location
            builder.location(location).spawn();
        } else {
            // Fallback for non-cached particles
            location.getWorld().spawnParticle(type, location, count, offsetX, offsetY, offsetZ, extra);
        }
    }

    /**
     * Spawn particles at a location using a custom builder.
     *
     * @param builder The particle builder to use
     */
    public static void spawnParticle(ParticleBuilder builder) {
        builder.spawn();
    }

    /**
     * Create a smoke particle trail effect.
     *
     * @param location The location to spawn at
     */
    public static void smokeTrail(Location location) {
        ParticleBuilder builder = Particle.SMOKE.builder()
                .location(location)
                .count(2)
                .offset(0.2, 0.2, 0.2)
                .extra(0);
        builder.spawn();
    }

    /**
     * Create a cloud burst effect (used for double jump).
     *
     * @param location The location to spawn at
     */
    public static void cloudBurst(Location location) {
        ParticleBuilder builder = Particle.CLOUD.builder()
                .location(location.add(0, 0.1, 0))
                .count(10)
                .offset(0.5, 0.2, 0.5)
                .extra(0);
        builder.spawn();
    }

    /**
     * Create an explosion effect (used for grenades).
     *
     * @param location The location to spawn at
     */
    public static void explosion(Location location) {
        ParticleBuilder builder = Particle.EXPLOSION.builder()
                .location(location)
                .count(5)
                .offset(0.5, 0.5, 0.5)
                .extra(0);
        builder.spawn();
    }

    /**
     * Play the spell casting sound.
     *
     * @param location The location to play at
     */
    public static void playCastSound(Location location) {
        playSound(location, Sound.ENTITY_EVOKER_CAST_SPELL, 0.4f, 1.0f);
    }

    /**
     * Play the jump sound.
     *
     * @param location The location to play at
     */
    public static void playJumpSound(Location location) {
        playSound(location, Sound.ENTITY_BREEZE_JUMP, 0.7f, 1.0f);
    }

    /**
     * Play the explosion sound.
     *
     * @param location The location to play at
     */
    public static void playExplosionSound(Location location) {
        playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    /**
     * Play the hit sound.
     *
     * @param location The location to play at
     */
    public static void playHitSound(Location location) {
        playSound(location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
    }
}
