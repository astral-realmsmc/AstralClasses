package com.astralrealms.classes.skill;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.state.BasicShootState;
import com.astralrealms.classes.util.Effects;
import com.astralrealms.classes.util.GameUtils;
import com.astralrealms.classes.visual.FireParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.time.Duration;
import java.util.*;

@ConfigSerializable
public record AdvancedShootSkill(int range, double damage, double helixRadius, double hitOffset,
                                 Duration cooldown) implements AttackSkill, CooldownSkill, Tickable {

    @Override
    public void tick() { }

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {

        BasicShootState state = BasicShootSkill.getState(player).orElse(null);
        if (state == null || state.hits() != 6)
            return;

        Location eyeLocation = GameUtils.getEyeLocation(player);
        eyeLocation.add(player.getLocation().getDirection().multiply(1.0));

        Effects.playSound(eyeLocation, Sound.ITEM_FIRECHARGE_USE, 0.4f, 1.0f);

        Vector direction = eyeLocation.getDirection();
        Vector perpendicular = getPerpendicularVector(direction);

        // Find targets
        TargetResult targets = findTargets(eyeLocation, direction);

        // Spawn helix effect
        FireParticle.spawnFireHelixEffect(eyeLocation.clone().add(0,0.3,0).add(direction.clone().multiply(0.5)), direction, perpendicular, helixRadius, targets != null ? targets.hitDistance : range);

        if(targets != null && !targets.hitEntities.isEmpty()){
            // Apply damage and effects
            double damage = GameUtils.computeDamage(player, inputType, this.damage);
            applyDamageAndEffects(player, targets.hitEntities, damage);
        }

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
        Location lastHitLocation = eyeLocation.clone();
        Collection<LivingEntity> potentialHits = GameUtils.raytraceEntities(eyeLocation, direction, range,
                2.0 * helixRadius, 0.3, lastHitLocation);

        if(potentialHits.isEmpty()){
            return null;
        }

        return new TargetResult(potentialHits.stream().toList(), eyeLocation.distance(lastHitLocation));
    }

    private void applyDamageAndEffects(Player player, Collection<LivingEntity> targets, double damage) {
        for (LivingEntity target : targets) {
            if (target instanceof Player)
                continue;

            target.damage(damage, DamageSource.builder(DamageType.MAGIC)
                    .withDirectEntity(player)
                    .build());

            target.setFireTicks(100);

            Effects.playHitSound(target.getLocation());
        }
    }

    private record TargetResult(List<LivingEntity> hitEntities, double hitDistance) {
    }

}