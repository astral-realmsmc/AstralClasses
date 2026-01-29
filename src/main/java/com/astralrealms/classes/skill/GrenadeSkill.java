package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.util.Effects;
import com.astralrealms.classes.util.GameUtils;
import com.destroystokyo.paper.ParticleBuilder;

@ConfigSerializable
public record GrenadeSkill(ItemStack item, double velocity, double impactRange, double damage,
                           double knockbackVelocity, Duration cooldown) implements AttackSkill, CooldownSkill {

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        Item grenade = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Item.class);
        grenade.setItemStack(item);

        // Launch the grenade forward with some upward velocity
        grenade.setVelocity(player.getEyeLocation().getDirection().multiply(velocity));

        // Compute damage
        double damage = GameUtils.computeDamage(player, inputType, this.damage);

        // When the grenade lands or hits an entity, create an explosion effect
        AtomicReference<Location> lastLocation = new AtomicReference<>(grenade.getLocation());
        long launchTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskTimer(AstralClasses.getPlugin(AstralClasses.class), (task) -> {
            Location currentLoc = grenade.getLocation();

            // Check if grenade should continue flying
            boolean shouldExplode = false;

            // Check if grenade hit the ground (block at grenade location is solid, or very close to ground)
            if (currentLoc.getBlock().getType().isSolid()) {
                shouldExplode = true;
            } else if (currentLoc.getY() <= currentLoc.getBlockY() + 0.2 &&
                       currentLoc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                // Grenade is touching or very close to the ground
                shouldExplode = true;
            }

            // Check if grenade hit an entity (within 0.5 blocks)
            if (!shouldExplode) {
                var nearbyEntities = grenade.getWorld()
                        .getNearbyEntities(currentLoc, 0.5, 0.5, 0.5);
                for (Entity entity : nearbyEntities) {
                    if (entity != grenade && entity != player) {
                        shouldExplode = true;
                        break;
                    }
                }
            }

            // Check timeout (3 seconds)
            if (!shouldExplode && System.currentTimeMillis() - launchTime >= 3000) {
                shouldExplode = true;
            }

            // Continue flying if no collision
            if (!shouldExplode) {
                ParticleBuilder smokeParticle = Particle.SMOKE.builder()
                        .location(currentLoc)
                        .count(5)
                        .offset(0.2, 0.2, 0.2)
                        .extra(0);
                smokeParticle.spawn();

                lastLocation.set(currentLoc.clone());
                return;
            }

            // Create expanding purple and white explosion effect
            createExplosionEffect(lastLocation.get(), impactRange);

            // Remove the grenade item
            grenade.remove();

            // Apply knockback and damage to nearby players
            List<Entity> nearbyEntities = grenade.getWorld()
                    .getNearbyEntities(grenade.getLocation(), impactRange, impactRange, impactRange)
                    .stream()
                    .toList();
            for (Entity entity : nearbyEntities) {
                entity.setVelocity(entity.getLocation().toVector().subtract(grenade.getLocation().toVector()).normalize().multiply(this.knockbackVelocity).setY(0.5));
                if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player))
                    livingEntity.damage(damage, DamageSource.builder(DamageType.MAGIC)
                            .withDirectEntity(player)
                            .build());
            }

            // Play sound
            Effects.playExplosionSound(lastLocation.get());

            task.cancel();
        }, 0L, 1L);
    }

    private static void createExplosionEffect(Location center, double maxRadius) {
        // Number of particles and expansion duration
        final int particlesPerRing = 30;
        final int rings = 8;
        final long delayPerRing = 2L;

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

                    // Alternate between purple and white
                    if (currentRing % 2 == 0) {
                        // Purple particles - use PORTAL for purple effect
                        Particle.PORTAL.builder()
                                .location(particleLoc)
                                .count(2)
                                .offset(0, 0, 0)
                                .extra(0)
                                .spawn();
                    } else {
                        // White particles - use CLOUD for white effect
                        Particle.CLOUD.builder()
                                .location(particleLoc)
                                .count(1)
                                .offset(0, 0, 0)
                                .extra(0)
                                .spawn();
                    }
                }

                // Add some rising particles in the center for depth
                for (int i = 0; i < 5; i++) {
                    double offset = Math.random() * radius * 0.3;
                    Location upwardLoc = center.clone().add(
                            (Math.random() - 0.5) * offset,
                            Math.random() * 0.5,
                            (Math.random() - 0.5) * offset
                    );

                    Particle.END_ROD.builder()
                            .location(upwardLoc)
                            .count(1)
                            .offset(0, 0.2, 0)
                            .extra(0.05)
                            .spawn();
                }

            }, ring * delayPerRing);
        }
    }

}
