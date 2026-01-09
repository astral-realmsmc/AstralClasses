package com.astralrealms.classes.skill;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.destroystokyo.paper.ParticleBuilder;

public class BasicShootSkill implements Skill {

    // TODO: Make this configurable

    @Override
    public void trigger(Player player, SkillContext context) {
        Location eyeLocation = player.getLocation().add(0, player.getEyeHeight(), 0);
        eyeLocation.add(player.getLocation().getDirection().multiply(1.0));

        // Spawn a line of particles from eye location to target location
        ParticleBuilder whiteParticle = Particle.DUST.builder()
                .color(Color.WHITE);
        ParticleBuilder purpleParticle = Particle.DUST.builder()
                .color(Color.PURPLE);

        for (double t = 0; t < 10.0; t += 0.3) {
            Location particleLocation = eyeLocation.clone().add(eyeLocation.getDirection().multiply(t));

            whiteParticle.location(particleLocation).spawn();
            purpleParticle.location(particleLocation.clone().add(0, 0.05, 0)).spawn();
        }

        Entity target = player.getTargetEntity(10);
        if (target != null) {
            // Apply velocity to the target entity in the direction the player is facing
            target.setVelocity(player.getLocation().getDirection().multiply(1.5));

            // Damage
            if (target instanceof LivingEntity livingEntity)
                livingEntity.damage(4.0, player);
        }
    }

}
