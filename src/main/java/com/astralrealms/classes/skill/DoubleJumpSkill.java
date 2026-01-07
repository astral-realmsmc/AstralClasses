package com.astralrealms.classes.skill;

import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.configuration.DoubleJumpConfiguration;

public class DoubleJumpSkill implements Skill {

    @Override
    public void trigger(Player player, Input input) {
        DoubleJumpConfiguration configuration = AstralClasses.getPlugin(AstralClasses.class).doubleJumpConfiguration();


        Vector direction = player.getLocation().getDirection();
        boolean isLeft = input.isLeft();
        boolean isRight = input.isRight();
        boolean isForward = input.isForward();
        boolean isBackward = input.isBackward();

        Vector jumpVector = new Vector(0, 1, 0); // Base upward jump
        boolean hasHorizontalInput = isLeft || isRight || isForward || isBackward;

        if (hasHorizontalInput) {
            Vector velocityMultiplier = configuration.horizontalVelocityMultiplier();
            if (isForward)
                jumpVector.add(direction.clone().setY(0).normalize().multiply(velocityMultiplier.getZ()));
            if (isBackward)
                jumpVector.add(direction.clone().setY(0).normalize().multiply(-velocityMultiplier.getZ()));
            if (isLeft) {
                Vector leftVector = direction.clone().setY(0).normalize().crossProduct(new Vector(0, 1, 0)).multiply(-velocityMultiplier.getX());
                jumpVector.add(leftVector);
            }
            if (isRight) {
                Vector rightVector = direction.clone().setY(0).normalize().crossProduct(new Vector(0, 1, 0)).multiply(velocityMultiplier.getX());
                jumpVector.add(rightVector);
            }
            jumpVector.multiply(velocityMultiplier);
        } else {
            Vector verticalMultiplier = configuration.verticalVelocityMultiplier();
            jumpVector.multiply(verticalMultiplier);
        }

        player.setVelocity(jumpVector);
    }

}
