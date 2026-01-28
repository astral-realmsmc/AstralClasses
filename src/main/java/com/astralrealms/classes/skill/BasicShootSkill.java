package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.ClassAPI;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.stat.StatType;
import com.astralrealms.classes.model.state.BasicShootState;
import com.astralrealms.core.paper.utils.ComponentUtils;
import com.destroystokyo.paper.ParticleBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@ConfigSerializable
public record BasicShootSkill(int range, double damage, double knockbackVelocity, double helixRadius, double hitOffset,
                              Duration cooldown) implements AttackSkill, CooldownSkill, Tickable, Listener {

    private static final Map<UUID, BasicShootState> states = new ConcurrentHashMap<>();
    private static final Component COMPLETED_BAR = Component.text("■", NamedTextColor.GREEN);
    private static final Component EMPTY_BAR = Component.text("□", NamedTextColor.GRAY);

    @Override
    public void tick() {
        for (Map.Entry<UUID, BasicShootState> entry : states.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                states.remove(entry.getKey());
                continue;
            }

            BasicShootState state = entry.getValue();
            // Tick the state
            state.tick();

            // Update action bar
            player.sendActionBar(ComponentUtils.progressBar((double) state.hits() / 6, 6, COMPLETED_BAR, EMPTY_BAR));
        }
    }

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        Location eyeLocation = player.getLocation().add(0, player.getEyeHeight() - 0.75, 0);
        eyeLocation.add(player.getLocation().getDirection().multiply(1.0));

        // Spawn a line of particles from eye location to target location
        ParticleBuilder whiteParticle = Particle.SMOKE.builder()
                .offset(0, 0, 0)
                .count(1)
                .extra(0f);
        ParticleBuilder purpleParticle = Particle.FLAME.builder()
                .offset(0, 0, 0)
                .count(1)
                .extra(0f);

        // Play sound
        player.getWorld().playSound(eyeLocation, Sound.ENTITY_EVOKER_CAST_SPELL, 0.4f, 1.0f);

        Vector direction = eyeLocation.getDirection();
        Vector perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        // If direction is parallel to Y-axis, use a different perpendicular vector
        if (perpendicular.lengthSquared() < 0.01) {
            perpendicular = direction.clone().crossProduct(new Vector(1, 0, 0)).normalize();
        }

        // Track hit entity to stop particles
        LivingEntity hitEntity = null;
        double hitDistance = range;

        // First pass: detect if any entity is hit and at what distance
        Collection<LivingEntity> potentialHits = raytraceEntities(eyeLocation, direction, range, 2.0 * helixRadius + hitOffset);
        if (!potentialHits.isEmpty()) {
            // Find the closest hit entity
            for (LivingEntity entity : potentialHits) {
                Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector());
                double distance = toEntity.dot(direction);
                // Ensure distance is positive and within range
                if (distance >= 0 && distance < hitDistance) {
                    hitDistance = distance;
                    hitEntity = entity;
                }
            }
        }

        // Second pass: spawn particles only up to the hit point
        for (double t = 0; t < hitDistance; t += 0.3) {
            Location baseLocation = eyeLocation.clone().add(direction.clone().multiply(t));

            // Calculate rotation angle based on distance
            double angle = t * 2; // Adjust multiplier to change spiral tightness

            // Calculate perpendicular offsets for the helix
            Vector rotatedPerp = rotateAroundAxis(perpendicular, direction, angle);
            Vector offset1 = rotatedPerp.clone().multiply(helixRadius);
            Vector offset2 = rotatedPerp.clone().multiply(-helixRadius);

            whiteParticle.location(baseLocation.clone().add(offset1)).spawn();
            purpleParticle.location(baseLocation.clone().add(offset2)).spawn();
        }

        // Compute damage stats
        double damage = ClassAPI.getStat(player, inputType, StatType.DAMAGE, this.damage);

        // Apply effects to hit entity (or all entities if no specific hit was found)
        Collection<LivingEntity> hitEntities = hitEntity != null ? List.of(hitEntity) : potentialHits;

        // Record hit in state
        if (!hitEntities.isEmpty() && hitEntities.stream().anyMatch(e -> !e.isDead() && e != player && !e.isInvulnerable()))
            editState(player, BasicShootState::recordHit);

        // Apply effects
        for (LivingEntity target : hitEntities) {
            target.setVelocity(player.getLocation().getDirection().multiply(knockbackVelocity));
            if (target instanceof Player)
                continue;

            // Damage
            target.damage(damage, player);

            // Play sound
            player.getWorld().playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

            // Glow effect
            target.setGlowing(true);
            Bukkit.getScheduler().runTaskLaterAsynchronously(AstralClasses.getPlugin(AstralClasses.class), () -> {
                if (!target.isDead())
                    target.setGlowing(false);
            }, 25L);
        }
    }

    private Collection<LivingEntity> raytraceEntities(Location start, Vector direction, double maxDistance, double hitRadius) {
        RayTraceResult rayTraceResult = start.getWorld().rayTraceEntities(start, direction, maxDistance, hitRadius, entity -> entity instanceof LivingEntity && !(entity instanceof Player));
        if (rayTraceResult != null && rayTraceResult.getHitEntity() != null)
            return List.of((LivingEntity) rayTraceResult.getHitEntity());
        return List.of();
    }

    // Helper method to rotate a vector around an axis
    private Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
        axis = axis.clone().normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = vector.dot(axis);

        return vector.clone()
                .multiply(cos)
                .add(axis.clone().crossProduct(vector).multiply(sin))
                .add(axis.clone().multiply(dot * (1 - cos)));
    }

    private void editState(Player player, Consumer<BasicShootState> consumer) {
        states.compute(player.getUniqueId(), (_, state) -> {
            if (state == null)
                state = new BasicShootState();
            consumer.accept(state);
            return state;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        states.remove(e.getPlayer().getUniqueId());
    }

    public static Optional<BasicShootState> getState(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }
}