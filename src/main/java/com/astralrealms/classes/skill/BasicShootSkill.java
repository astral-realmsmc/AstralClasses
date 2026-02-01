package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.astralrealms.classes.visual.FireParticle;
import com.astralrealms.classes.visual.GeneralParticle;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@ConfigSerializable
public record BasicShootSkill(int range, double damage, double knockbackVelocity, double helixRadius, double hitOffset,
                              Duration cooldown) implements AttackSkill, CooldownSkill, Tickable {

    private static final StateCache<BasicShootState> states = new StateCache<>();
    private static final Component COMPLETED_BAR = Component.text("■", NamedTextColor.GREEN);
    private static final Component EMPTY_BAR = Component.text("□", NamedTextColor.GRAY);
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

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

        // Find targets
        TargetResult target = findTarget(eyeLocation, direction);

        // Spawn helix effect
        FireParticle.spawnFireTrail(eyeLocation, direction, target != null ? target.hitDistance : range);

        if(target != null && target.hitEntity != null){
            // Apply damage and effects
            double damage = GameUtils.computeDamage(player, inputType, this.damage);
            applyDamageAndEffects(player, target.hitEntity, damage);

            // Update hit markers
            states.edit(player.getUniqueId(), BasicShootState::new, BasicShootState::recordHit);

        }
    }

    private TargetResult findTarget(Location eyeLocation, Vector direction) {

        LivingEntity potentialHit = GameUtils.raytraceEntity(eyeLocation, direction, range,
                2.0 * helixRadius + hitOffset);

        if(potentialHit == null){
            return null;
        }

        return new TargetResult(potentialHit, eyeLocation.distance(potentialHit.getLocation()));
    }

    private void applyDamageAndEffects(Player player, LivingEntity target, double damage) {
        target.setVelocity(player.getLocation().getDirection().multiply(knockbackVelocity));

        target.damage(damage, DamageSource.builder(DamageType.MAGIC)
                .withDirectEntity(player)
                .build());

        Effects.playHitSound(target.getLocation());

        // Play hit particle effect
        GeneralParticle.spawnHitParticleEffect(target.getLocation());
    }

    private record TargetResult(LivingEntity hitEntity, double hitDistance) {
    }

    public static Optional<BasicShootState> getState(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }
}