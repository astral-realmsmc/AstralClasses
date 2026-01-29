package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.state.BasicShootState;
import com.astralrealms.classes.util.Effects;
import com.astralrealms.classes.util.GameUtils;
import com.astralrealms.classes.util.StateCache;
import com.astralrealms.core.paper.utils.ComponentUtils;
import com.destroystokyo.paper.ParticleBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@ConfigSerializable
public record BasicShootSkill(int range, double damage, double knockbackVelocity, double helixRadius, double hitOffset,
                              Duration cooldown) implements AttackSkill, CooldownSkill, Tickable {

    private static final StateCache<BasicShootState> states = new StateCache<>();
    private static final Component COMPLETED_BAR = Component.text("■", NamedTextColor.GREEN);
    private static final Component EMPTY_BAR = Component.text("□", NamedTextColor.GRAY);

    @Override
    public void tick() {
        // Iterate through cached states
        for (UUID uuid : states.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                states.remove(uuid);
                continue;
            }

            BasicShootState state = states.get(uuid);
            if (state == null)
                continue;

            // Tick the state
            state.tick();

            // Update action bar
            player.sendActionBar(ComponentUtils.progressBar((double) state.hits() / 6, 6, COMPLETED_BAR, EMPTY_BAR));
        }
    }

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        Location eyeLocation = GameUtils.getEyeLocation(player);
        eyeLocation.add(player.getLocation().getDirection().multiply(1.0));

        Effects.playCastSound(eyeLocation);

        Vector direction = eyeLocation.getDirection();
        Vector perpendicular = getPerpendicularVector(direction);

        // Find targets
        TargetResult targets = findTargets(eyeLocation, direction);

        // Spawn helix effect
        spawnHelixEffect(eyeLocation, direction, perpendicular, targets.hitDistance);

        // Apply damage and effects
        double damage = GameUtils.computeDamage(player, inputType, this.damage);
        applyDamageAndEffects(player, targets.hitEntities, damage);

        // Update hit markers
        if (!targets.hitEntities.isEmpty() && hasValidHit(targets.hitEntities, player))
            states.edit(player.getUniqueId(), BasicShootState::new, BasicShootState::recordHit);
    }

    private Vector getPerpendicularVector(Vector direction) {
        Vector perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        // If direction is parallel to Y-axis, use a different perpendicular vector
        if (perpendicular.lengthSquared() < 0.01) {
            perpendicular = direction.clone().crossProduct(new Vector(1, 0, 0)).normalize();
        }
        return perpendicular;
    }

    private TargetResult findTargets(Location eyeLocation, Vector direction) {
        double hitDistance = range;
        LivingEntity hitEntity = null;

        Collection<LivingEntity> potentialHits = GameUtils.raytraceEntities(eyeLocation, direction, range,
                2.0 * helixRadius + hitOffset);

        if (!potentialHits.isEmpty()) {
            // Find the closest hit entity that has line of sight
            for (LivingEntity entity : potentialHits) {
                // Check if there's a clear line of sight (no blocks in the way)
                if (!eyeLocation.hasLineOfSight(entity.getLocation())) {
                    continue;
                }

                Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector());
                double distance = toEntity.dot(direction);
                // Ensure distance is positive and within range
                if (distance >= 0 && distance < hitDistance) {
                    hitDistance = distance;
                    hitEntity = entity;
                }
            }
        }

        Collection<LivingEntity> hitEntities = hitEntity != null ? List.of(hitEntity) : potentialHits;
        return new TargetResult(hitEntities, hitEntity, hitDistance);
    }

    private void spawnHelixEffect(Location eyeLocation, Vector direction, Vector perpendicular, double maxDistance) {
        ParticleBuilder whiteParticle = Particle.SMOKE.builder()
                .offset(0, 0, 0)
                .count(1)
                .extra(0f);
        ParticleBuilder purpleParticle = Particle.FLAME.builder()
                .offset(0, 0, 0)
                .count(1)
                .extra(0f);

        // Spawn particles up to the hit point
        for (double t = 0; t < maxDistance; t += 0.3) {
            Location baseLocation = eyeLocation.clone().add(direction.clone().multiply(t));

            // Calculate rotation angle based on distance
            double angle = t * 2;

            // Calculate perpendicular offsets for the helix
            Vector rotatedPerp = GameUtils.rotateAroundAxis(perpendicular, direction, angle);
            Vector offset1 = rotatedPerp.clone().multiply(helixRadius);
            Vector offset2 = rotatedPerp.clone().multiply(-helixRadius);

            whiteParticle.location(baseLocation.clone().add(offset1)).spawn();
            purpleParticle.location(baseLocation.clone().add(offset2)).spawn();
        }
    }

    private void applyDamageAndEffects(Player player, Collection<LivingEntity> targets, double damage) {
        for (LivingEntity target : targets) {
            target.setVelocity(player.getLocation().getDirection().multiply(knockbackVelocity));
            if (target instanceof Player)
                continue;

            target.damage(damage, DamageSource.builder(DamageType.MAGIC)
                    .withDirectEntity(player)
                    .build());

            Effects.playHitSound(target.getLocation());

            target.setGlowing(true);
            Bukkit.getScheduler().runTaskLaterAsynchronously(AstralClasses.getPlugin(AstralClasses.class), () -> {
                if (!target.isDead())
                    target.setGlowing(false);
            }, 25L);
        }
    }

    private boolean hasValidHit(Collection<LivingEntity> targets, Player player) {
        return targets.stream().anyMatch(e -> !e.isDead() && e != player && !e.isInvulnerable());
    }

    private record TargetResult(Collection<LivingEntity> hitEntities, LivingEntity hitEntity, double hitDistance) {
    }

    public static Optional<BasicShootState> getState(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }
}