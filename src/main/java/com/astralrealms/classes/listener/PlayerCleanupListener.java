package com.astralrealms.classes.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Centralized listener for player quit cleanup.
 * Eliminates duplication of @EventHandler onQuit methods across skills.
 * Components register cleanup handlers that are called when players leave.
 */
public class PlayerCleanupListener implements Listener {

    private final List<Consumer<UUID>> cleanupHandlers = new ArrayList<>();

    /**
     * Register a cleanup handler to be called when a player quits.
     *
     * @param handler The handler to call with the player's UUID
     */
    public void registerHandler(Consumer<UUID> handler) {
        cleanupHandlers.add(handler);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Call all registered cleanup handlers
        for (Consumer<UUID> handler : cleanupHandlers) {
            try {
                handler.accept(uuid);
            } catch (Exception e) {
                // Log but don't prevent other handlers from running
                e.printStackTrace();
            }
        }
    }
}
