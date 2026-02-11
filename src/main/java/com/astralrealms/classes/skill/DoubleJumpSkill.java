package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.UUID;

import org.bukkit.Input;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.context.InputSkillContext;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.state.JumpState;
import com.astralrealms.classes.utils.Effects;
import com.astralrealms.classes.utils.StateCache;

@ConfigSerializable
public record DoubleJumpSkill(Vector verticalVelocityMultiplier, Vector horizontalVelocityMultiplier,
                              Duration cooldown) implements Skill, Listener {

    private static final StateCache<JumpState> jumpStates = new StateCache<>();

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        if (!(context instanceof InputSkillContext(Input input)))
            throw new IllegalArgumentException("Expected InputSkillContext for DoubleJumpSkill");
        if (!input.isJump())
            return;

        // Get or create jump state for this player
        JumpState state = jumpStates.getOrCompute(player.getUniqueId(), JumpState::new);

        // Player on ground = first jump
        if (isOnGround(player)) {
            state.reset();
            state.recordJump();
            return;
        } else if (!state.canDoubleJump(System.currentTimeMillis(), this.cooldown.toMillis(), 1)) {
            return;
        }

        // Spawn particle effect at player's feet location
        Effects.cloudBurst(player.getLocation());

        // Apply velocity
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        boolean isLeft = input.isLeft();
        boolean isRight = input.isRight();
        boolean isForward = input.isForward();
        boolean isBackward = input.isBackward();

        Vector jumpVector = new Vector(0, 1, 0); // Base upward jump
        boolean hasHorizontalInput = isLeft || isRight || isForward || isBackward;

        if (hasHorizontalInput) {
            Vector velocityMultiplier = this.horizontalVelocityMultiplier;

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
            jumpVector.multiply(this.verticalVelocityMultiplier);
        }

        player.setVelocity(jumpVector);

        state.recordDoubleJump(System.currentTimeMillis());

        // Play sound
        Effects.playJumpSound(player.getLocation());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Early exit: only process when moving between blocks
        if (!event.hasChangedBlock())
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Reset jump state when player touches ground
        if (!isOnGround(player))
            return;

        JumpState state = jumpStates.get(uuid);
        if (state != null && state.jumpCount() > 0)
            state.reset();
    }

    private boolean isOnGround(Player player) {
        BoundingBox boundingBox = player.getBoundingBox();
        Block blockBelow = player.getWorld().getBlockAt((int) boundingBox.getMinX(), (int) (boundingBox.getMinY() - 1), (int) boundingBox.getMinZ());
        return blockBelow.isCollidable();
    }
}
