package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.utils.Effects;
import com.astralrealms.classes.utils.GameUtils;
import com.destroystokyo.paper.ParticleBuilder;

import net.kyori.adventure.text.format.NamedTextColor;

@ConfigSerializable
public record GrenadeSkill(ItemStack item, double velocity, double impactRange, double damage,
                           Vector playerKnockbackVelocity, double entityKnockbackVelocity,
                           Duration cooldown,
                           net.kyori.adventure.sound.Sound sound) implements AttackSkill, CooldownSkill {

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        Item grenade = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Item.class);
        grenade.setItemStack(item);
        Team grenadeTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("grenades");
        if (grenadeTeam != null) {
            grenadeTeam.addEntities(grenade);
        } else {
            grenadeTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("grenades");
            grenadeTeam.color(NamedTextColor.RED);
            grenadeTeam.addEntities(grenade);
        }
        grenade.setGlowing(true);

        // Launch the grenade forward with some upward velocity
        grenade.setVelocity(player.getEyeLocation().getDirection().multiply(velocity));

        // Compute damage
        double damage = GameUtils.computeDamage(player, inputType, this.damage);

        Effects.playSound(player.getEyeLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 0.4f, 1.0f);
        Effects.playSound(player.getEyeLocation(), Sound.ENTITY_TNT_PRIMED, 0.4f, 1.0f);

        // When the grenade lands or hits an entity, create an explosion effect
        AtomicReference<Location> lastLocation = new AtomicReference<>(grenade.getLocation().clone());
        long launchTime = System.currentTimeMillis();
        Team finalGrenadeTeam = grenadeTeam;

        Bukkit.getScheduler().runTaskTimer(AstralClasses.getPlugin(AstralClasses.class), (task) -> {
            Location currentLoc = grenade.getLocation();
            Location prevLoc = lastLocation.get();

            // Check if grenade should continue flying
            AtomicReference<Boolean> hitEntity = new AtomicReference<>(null);
            boolean shouldExplode = false;
            Location explosionLoc = currentLoc.clone();

            // === IMPROVED COLLISION DETECTION ===

            // 1. Check timeout first (3 seconds)
            if (System.currentTimeMillis() - launchTime >= 3000) {
                shouldExplode = true;
                hitEntity.set(false);
            }

            // 2. Raycast between previous and current position to prevent pass-through
            if (!shouldExplode) {
                Vector direction = currentLoc.toVector().subtract(prevLoc.toVector());
                double distance = direction.length();

                if (distance > 0.05) { // Only raycast if grenade moved significantly
                    direction.normalize();

                    // Check each 0.1 block step along the path
                    for (double d = 0; d <= distance; d += 0.1) {
                        Location checkLoc = prevLoc.clone().add(direction.clone().multiply(d));

                        // Check for solid blocks
                        if (checkLoc.getBlock().getType().isSolid()) {
                            shouldExplode = true;
                            hitEntity.set(false);
                            explosionLoc = checkLoc;
                            break;
                        }

                        // Check for entities with larger radius
                        var nearbyEntities = grenade.getWorld()
                                .getNearbyEntities(checkLoc, 0.8, 0.8, 0.8);
                        for (Entity entity : nearbyEntities) {
                            if (entity != grenade && entity != player && entity instanceof LivingEntity) {
                                shouldExplode = true;
                                hitEntity.set(true);
                                explosionLoc = checkLoc;
                                break;
                            }
                        }

                        if (shouldExplode) break;
                    }
                }
            }

            // 3. Final position checks (ground proximity)
            if (!shouldExplode) {
                // Check if grenade is at current position on solid block
                if (currentLoc.getBlock().getType().isSolid()) {
                    shouldExplode = true;
                    hitEntity.set(false);
                }
                // Check if very close to ground
                else if (currentLoc.getY() <= currentLoc.getBlockY() + 0.2 &&
                         currentLoc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                    shouldExplode = true;
                    hitEntity.set(false);
                }
                // Final entity check at current position
                else {
                    var nearbyEntities = grenade.getWorld()
                            .getNearbyEntities(currentLoc, 0.8, 0.8, 0.8);
                    for (Entity entity : nearbyEntities) {
                        if (entity != grenade && entity != player && entity instanceof LivingEntity) {
                            shouldExplode = true;
                            hitEntity.set(true);
                            break;
                        }
                    }
                }
            }

            // Continue flying if no collision
            if (!shouldExplode) {
                ParticleBuilder smokeParticle = Particle.TRIAL_SPAWNER_DETECTION.builder()
                        .location(currentLoc)
                        .count(5)
                        .offset(0.2, 0.2, 0.2)
                        .extra(0);
                smokeParticle.spawn();

                lastLocation.set(currentLoc.clone());
                return;
            }

            // === EXPLOSION ===

            // Create expanding purple and white explosion effect
            createExplosionEffect(player, explosionLoc, impactRange, hitEntity.get());

            // Remove the grenade item
            finalGrenadeTeam.removeEntities(grenade);
            grenade.remove();

            // Damage nearby entities (excluding players)
            List<Entity> nearbyEntities = grenade.getWorld()
                    .getNearbyEntities(explosionLoc, impactRange, impactRange, impactRange)
                    .stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                    .toList();

            for (Entity entity : nearbyEntities) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(damage, DamageSource.builder(DamageType.MAGIC)
                        .withDirectEntity(player)
                        .build());
            }

            // Apply knockback to nearby entities
            for (Entity entity : nearbyEntities) {
                LivingEntity livingEntity = (LivingEntity) entity;
                Vector knockbackDirection = livingEntity.getLocation().toVector()
                        .subtract(explosionLoc.toVector())
                        .setY(0)
                        .normalize();
                Vector knockback = knockbackDirection.multiply(entityKnockbackVelocity);
                livingEntity.setVelocity(livingEntity.getVelocity().add(knockback));
            }

            // Apply knockback to the player depending on their position relative to the explosion
            if (player.getLocation().distance(explosionLoc) <= impactRange * 1.3) {
                Vector playerKnockback = player.getLocation()
                        .getDirection()
                        .normalize()
                        .multiply(-playerKnockbackVelocity.length());
                player.setVelocity(player.getVelocity().add(playerKnockback));
            }

            // Play sound
            player.playSound(sound);

            task.cancel();
        }, 0L, 1L);
    }

    private static void createExplosionEffect(Player player, Location center, double maxRadius, Boolean hitEntity) {
        if (hitEntity != null && hitEntity) {
            // Spherical explosion for entity hits (mid-air explosion)
            createSphericalExplosion(center, maxRadius);
        } else {
            // Ring-based explosion for ground hits
            createGroundExplosion(center, maxRadius);
        }

        // Play explosion sound
        float soundVolume = hitEntity != null && hitEntity ? 1.0f : 0.6f;
        float pitch = hitEntity != null && hitEntity ? 1.2f : 0.8f;

        double distance = player.getLocation().distance(center);
        if (distance > maxRadius * 2) {
            soundVolume *= 0.5f; // Distant explosion sounds quieter
        } else if (distance > maxRadius) {
            soundVolume *= 0.8f; // Mid-range explosion slightly quieter
        }
        soundVolume = Math.max(0.1f, soundVolume * (1 - (float) (distance / (maxRadius * 2)))); // Linear falloff

        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, soundVolume, pitch);

    }

    private static void createSphericalExplosion(Location center, double maxRadius) {
        // Number of particles and expansion duration
        final int particlesPerSphere = 40;
        final int spheres = 6;
        final long delayPerSphere = 1L;

        ParticleBuilder flameRedOrangeParticle = Particle.DUST_COLOR_TRANSITION.builder()
                .colorTransition(226, 56, 34, 226, 120, 34)
                .offset(0.1, 0.2, 0.1)
                .count(2)
                .extra(0f);
        ParticleBuilder flameRedParticle = Particle.DUST.builder()
                .color(250, 35, 13)
                .offset(0.1, 0.2, 0.1)
                .count(2)
                .extra(0f);
        ParticleBuilder flameParticle = Particle.SMALL_FLAME.builder()
                .offset(0.1, 0.2, 0.1)
                .count(2)
                .extra(0f);
        ParticleBuilder dripParticle = Particle.CRIMSON_SPORE.builder()
                .offset(0.2, 0.3, 0.2)
                .count(1)
                .extra(0f);

        List<ParticleBuilder> particles = new ArrayList<>(List.of(flameRedOrangeParticle, flameParticle, flameRedParticle, dripParticle));

        ParticleBuilder gust = Particle.ITEM.builder()
                .data(new ItemStack(Material.MAGMA_BLOCK, 1))
                .offset(0.1, 0.4, 0.1)
                .count(2)
                .extra(0.3);
        ParticleBuilder lava = Particle.LAVA.builder()
                .offset(0.1, 0.1, 0.1)
                .count(2)
                .extra(0.05);

        List<ParticleBuilder> centerParticles = new ArrayList<>(List.of(lava, gust));

        for (int sphere = 0; sphere < spheres; sphere++) {
            final int currentSphere = sphere;
            final double radius = (maxRadius / spheres) * (sphere + 1);

            Bukkit.getScheduler().runTaskLater(AstralClasses.getPlugin(AstralClasses.class), () -> {
                // Create expanding sphere of particles using spherical coordinates
                for (int i = 0; i < particlesPerSphere; i++) {
                    // Golden angle for even distribution on sphere
                    double theta = i * 2.399963; // Golden angle in radians
                    double phi = Math.acos(1 - 2.0 * (i + 0.5) / particlesPerSphere);

                    double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
                    double y = center.getY() + radius * Math.cos(phi);
                    double z = center.getZ() + radius * Math.sin(phi) * Math.sin(theta);

                    Location particleLoc = new Location(center.getWorld(), x, y, z);

                    // Alternate between particle types
                    Collections.shuffle(particles);
                    particles.getFirst()
                            .location(particleLoc)
                            .spawn();
                }

                // Add some sparks radiating outward
                for (int i = 0; i < 8; i++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = Math.acos(2 * Math.random() - 1);

                    Vector direction = new Vector(
                            Math.sin(phi) * Math.cos(theta),
                            Math.cos(phi),
                            Math.sin(phi) * Math.sin(theta)
                    );

                    Location sparkLoc = center.clone().add(direction.multiply(radius));

                    Collections.shuffle(centerParticles);
                    centerParticles.getFirst()
                            .location(sparkLoc)
                            .spawn();
                }

            }, sphere * delayPerSphere);
        }
    }

    private static void createGroundExplosion(Location center, double maxRadius) {
        // Number of particles and expansion duration
        final int particlesPerRing = 15;
        final int rings = 8;
        final long delayPerRing = 0L;

        ParticleBuilder flameRedOrangeParticle = Particle.DUST_COLOR_TRANSITION.builder()
                .colorTransition(226, 56, 34, 226, 120, 34)
                .offset(0.1, 0.2, 0.1)
                .count(2)
                .extra(0f);
        ParticleBuilder flameRedParticle = Particle.DUST.builder()
                .color(250, 35, 13)
                .offset(0.1, 0.2, 0.1)
                .count(2)
                .extra(0f);
        ParticleBuilder flameParticle = Particle.SMALL_FLAME.builder()
                .offset(0.1, 0.2, 0.1)
                .count(2)
                .extra(0f);
        ParticleBuilder dripParticle = Particle.CRIMSON_SPORE.builder()
                .offset(0.2, 0.3, 0.2)
                .count(1)
                .extra(0f);

        List<ParticleBuilder> particles = new ArrayList<>(List.of(flameRedOrangeParticle, flameParticle, flameRedParticle, dripParticle));

        ParticleBuilder gust = Particle.ITEM.builder()
                .data(new ItemStack(Material.MAGMA_BLOCK, 1))
                .offset(0.1, 0.4, 0.1)
                .count(2)
                .extra(0.3);
        ParticleBuilder lava = Particle.LAVA.builder()
                .offset(0.1, 0.1, 0.1)
                .count(2)
                .extra(0.05);

        List<ParticleBuilder> centerParticles = new ArrayList<>(List.of(lava, gust));

        for (int ring = 0; ring < rings; ring++) {
            final int currentRing = ring;
            final double radius = (maxRadius / rings) * (ring + 1);

            Bukkit.getScheduler().runTaskLater(AstralClasses.getPlugin(AstralClasses.class), () -> {
                // Create expanding ring of particles
                for (int i = 0; i < particlesPerRing; i++) {
                    double angle = (2 * Math.PI * i) / particlesPerRing;
                    double x = center.getX() + radius * Math.cos(angle);
                    double y = center.getY() + (Math.random() - 0.5) * radius * 0.5; // Slight vertical spread
                    double z = center.getZ() + radius * Math.sin(angle);

                    Location particleLoc = new Location(center.getWorld(), x, y, z);

                    // Alternate between particle types
                    Collections.shuffle(particles);
                    particles.getFirst()
                            .location(particleLoc)
                            .spawn();

                }

                // Add some rising particles in the center for depth
                for (int i = 0; i < 4; i++) {
                    double offset = Math.random() * radius * 0.3;
                    Location upwardLoc = center.clone().add(
                            (Math.random() - 0.5) * offset,
                            Math.random() * 0.5,
                            (Math.random() - 0.5) * offset
                    );

                    Collections.shuffle(centerParticles);
                    centerParticles.getFirst()
                            .location(upwardLoc)
                            .spawn();
                }

            }, ring * delayPerRing);
        }
    }

}
