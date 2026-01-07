package com.astralrealms.classes.skill;

import org.bukkit.Input;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.configuration.DoubleJumpConfiguration;
import com.astralrealms.classes.model.skill.Skill;

public class DoubleJumpSkill implements Skill {

    @Override
    public void trigger(Player player, Input input) {
        DoubleJumpConfiguration configuration = AstralClasses.getPlugin(AstralClasses.class).doubleJumpConfiguration();

        // Spawn particle effect at player's feet location
        Particle.CLOUD.builder()
                .count(10)
                .offset(0, 0, 0)
                .location(player.getLocation().add(0, 0.1, 0))
                .spawn();

        // Reset fall distance to prevent fall damage
        player.setFallDistance(0);

        // Apply velocity
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        boolean isLeft = input.isLeft();
        boolean isRight = input.isRight();
        boolean isForward = input.isForward();
        boolean isBackward = input.isBackward();

        Vector jumpVector = new Vector(0, 1, 0); // Base upward jump
        boolean hasHorizontalInput = isLeft || isRight || isForward || isBackward;

        if (hasHorizontalInput) {
            Vector velocityMultiplier = configuration.horizontalVelocityMultiplier();

            // Calculate the right vector (perpendicular to direction)
            Vector rightVector = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

            if (isForward)
                jumpVector.add(direction.clone().multiply(velocityMultiplier.getZ()));
            if (isBackward)
                jumpVector.add(direction.clone().multiply(-velocityMultiplier.getZ()));
            if (isLeft)
                jumpVector.add(rightVector.clone().multiply(-velocityMultiplier.getX()));
            if (isRight)
                jumpVector.add(rightVector.clone().multiply(velocityMultiplier.getX()));

            jumpVector.multiply(velocityMultiplier);
        } else {
            Vector verticalMultiplier = configuration.verticalVelocityMultiplier();
            jumpVector.multiply(verticalMultiplier);
        }

        player.setVelocity(jumpVector);
    }

}
