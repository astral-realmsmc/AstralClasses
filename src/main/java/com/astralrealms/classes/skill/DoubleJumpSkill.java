package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Input;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.context.InputSkillContext;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.state.JumpState;

@ConfigSerializable
public record DoubleJumpSkill(Vector verticalVelocityMultiplier,
                              Vector horizontalVelocityMultiplier, Duration cooldown, int maxConsecutiveJumps) implements Skill, Listener {

    private static final Map<UUID, JumpState> jumpStates = new ConcurrentHashMap<>();

    @Override
    public void trigger(Player player, SkillContext context) {
        if (!(context instanceof InputSkillContext(Input input)))
            throw new IllegalArgumentException("Expected InputSkillContext for DoubleJumpSkill");
        if (!input.isJump())
            return;

        // Get or create jump state for this player
        JumpState state = jumpStates.computeIfAbsent(player.getUniqueId(), _ -> new JumpState());

        // Player on ground = first jump
        if (isOnGround(player)) {
            state.reset();
            state.recordJump();
            return;
        } else if (!state.canDoubleJump(System.currentTimeMillis(), 500, 1)) { // long currentTime, long cooldownMs, int maxJumps
            return;
        }

        // Spawn particle effect at player's feet location
        Particle.CLOUD.builder()
                .count(20)
                .offset(0.5, 0.2, 0.5)
                .extra(0)
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        jumpStates.remove(event.getPlayer().getUniqueId());
    }

    private boolean isOnGround(Player player) {
        Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
        return block.getType().isSolid();
    }
}
