package com.astralrealms.classes.model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;

/**
 * Buffers input states to detect state changes (false -> true).
 * Only triggers when an input actually changes state, not when it remains the same.
 */
public class InputBuffer {

    private final Map<UUID, BufferedInput> playerStates = new ConcurrentHashMap<>();

    /**
     * Checks if an input state has changed from false to true.
     * Updates the buffer and returns true only on rising edge (false -> true).
     *
     * @param player The player to check
     * @param input  The input type to check
     * @param packet The current input packet (for SPACE input)
     * @return true if the input changed from false to true, false otherwise
     */
    public boolean onInputReceived(Player player, InputType input, WrapperPlayClientPlayerInput packet) {
        if (input != InputType.SPACE && input != InputType.SNEAK) {
            return true; // Non-space inputs always trigger
        }

        UUID uuid = player.getUniqueId();
        BufferedInput currentState = playerStates.computeIfAbsent(uuid, _ -> new BufferedInput());

        if (input == InputType.SNEAK) {
            boolean newSneakState = packet.isShift();
            boolean previousSneakState = currentState.sneak;
            // Update the state
            currentState.sneak = newSneakState;
            // Only trigger on rising edge (false -> true)
            return !previousSneakState && newSneakState;
        }

        boolean newJumpState = packet.isJump();
        boolean previousJumpState = currentState.jump;

        // Update the state
        currentState.jump = newJumpState;

        // Only trigger on rising edge (false -> true)
        return !previousJumpState && newJumpState;
    }

    /**
     * Removes a player from the buffer (should be called on disconnect).
     */
    public void removePlayer(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    /**
     * Internal class to track buffered input states for a player.
     */
    private static class BufferedInput {
        boolean jump;
        boolean sneak;
    }
}
