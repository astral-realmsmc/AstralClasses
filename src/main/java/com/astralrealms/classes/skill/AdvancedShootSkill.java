package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import com.astralrealms.classes.utils.Effects;
import com.astralrealms.classes.utils.GameUtils;
import com.astralrealms.classes.visual.FireParticle;

@ConfigSerializable
public record AdvancedShootSkill(int range, double damage, double helixRadius, double hitOffset,
                                 Duration cooldown, double chargeDamage,
                                 int maxChargeDamage, net.kyori.adventure.sound.Sound sound) implements AttackSkill, CooldownSkill, Tickable {

    @Override
    public void tick() {
    }

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        BasicShootState state = BasicShootSkill.getState(player).orElse(null);
        if (state == null || state.hits() == 0)
            return;

        Location eyeLocation = GameUtils.getEyeLocation(player);
        eyeLocation.add(player.getLocation().getDirection().multiply(1.0));

        Effects.playSound(eyeLocation, Sound.ITEM_FIRECHARGE_USE, 0.4f, 1.0f);

        Vector direction = eyeLocation.getDirection();
        Vector perpendicular = getPerpendicularVector(direction);

        // Play sound
        player.playSound(sound);

        // Find targets
        TargetResult targets = findTargets(eyeLocation, direction);

        // Spawn helix effect
        Bukkit.getScheduler().runTaskAsynchronously(AstralClasses.instance(), () -> FireParticle.spawnFireHelixEffect(eyeLocation.clone().add(0, 0.3, 0).add(direction.clone().multiply(0.5)), direction, perpendicular, helixRadius, targets != null ? targets.hitDistance : range));

        // Determine hits to consume
        int hits = state.hits();
        int consumedHits = Math.min(hits, 4);

        // Consume hits
        state.updateHits(h -> h - consumedHits);

        if (targets == null || targets.hitEntities.isEmpty())
            return;

        // Apply damage and effects
        double damage = Math.min(GameUtils.computeDamage(player, inputType, this.damage) + (chargeDamage * consumedHits), maxChargeDamage);
        applyDamageAndEffects(player, targets.hitEntities, damage);
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
        List<LivingEntity> potentialHits = GameUtils.raytraceEntities(eyeLocation, direction, range,
                2.0 * helixRadius, 0.3, lastHitLocation);
        if (potentialHits == null || potentialHits.isEmpty())
            return null;
        return new TargetResult(potentialHits, eyeLocation.distance(lastHitLocation));
    }

    private void applyDamageAndEffects(Player player, Collection<LivingEntity> targets, double damage) {
        for (LivingEntity target : targets) {
            if (target instanceof Player)
                continue;

            target.damage(damage, DamageSource.builder(DamageType.MAGIC)
                    .withDirectEntity(player)
                    .build());

            target.setFireTicks(10);

            Effects.playHitSound(target.getLocation());
        }
    }

    private record TargetResult(List<LivingEntity> hitEntities, double hitDistance) {
    }

}