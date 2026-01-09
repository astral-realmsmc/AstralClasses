package com.astralrealms.classes.skill;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.destroystokyo.paper.ParticleBuilder;

public record BasicShootSkill(int range, int damage, double knockbackVelocity) implements Skill {

    @Override
    public void trigger(Player player, SkillContext context) {
        Location eyeLocation = player.getLocation().add(0, player.getEyeHeight(), 0);
        eyeLocation.add(player.getLocation().getDirection().multiply(1.0));

        // Spawn a line of particles from eye location to target location
        ParticleBuilder whiteParticle = Particle.DUST.builder()
                .color(Color.WHITE);
        ParticleBuilder purpleParticle = Particle.DUST.builder()
                .color(Color.PURPLE);

        Vector direction = eyeLocation.getDirection();
        Vector perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        // If direction is parallel to Y-axis, use a different perpendicular vector
        if (perpendicular.lengthSquared() < 0.01) {
            perpendicular = direction.clone().crossProduct(new Vector(1, 0, 0)).normalize();
        }

        double helixRadius = 0.3; // Adjust this to change how far apart the spirals are

        for (double t = 0; t < range; t += 0.3) {
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

        Entity target = player.getTargetEntity(range);
        if (target != null) {
            // Apply velocity to the target entity in the direction the player is facing
            target.setVelocity(player.getLocation().getDirection().multiply(knockbackVelocity));

            // Damage
            if (target instanceof LivingEntity livingEntity && !(target instanceof Player))
                livingEntity.damage(damage, player);
        }
    }

    // Helper method to rotate a vector around an axis
    private Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
        axis = axis.clone().normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = vector.dot(axis);

        return vector.clone().multiply(cos)
                .add(axis.clone().crossProduct(vector).multiply(sin))
                .add(axis.clone().multiply(dot * (1 - cos)));
    }

}
