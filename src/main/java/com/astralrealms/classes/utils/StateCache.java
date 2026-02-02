package com.astralrealms.classes.utils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Generic state management helper for per-player state.
 * Eliminates duplicated ConcurrentHashMap handling and quit event listeners.
 *
 * @param <T> The type of state being managed
 */
public final class StateCache<T> {

    private final Map<UUID, T> cache = new ConcurrentHashMap<>();

    /**
     * Get the state for a player, computing it if not present.
     *
     * @param uuid    The player's UUID
     * @param factory The factory to create the state if not present
     * @return The state for the player
     */
    public T getOrCompute(UUID uuid, Supplier<T> factory) {
        return cache.computeIfAbsent(uuid, _ -> factory.get());
    }

    /**
     * Get the state for a player without computing if absent.
     *
     * @param uuid The player's UUID
     * @return The state, or null if not present
     */
    public T get(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Set the state for a player.
     *
     * @param uuid  The player's UUID
     * @param state The state to set
     */
    public void set(UUID uuid, T state) {
        cache.put(uuid, state);
    }

    /**
     * Remove the state for a player.
     *
     * @param uuid The player's UUID
     */
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Apply a modification to the state for a player, creating it if not present.
     *
     * @param uuid     The player's UUID
     * @param factory  The factory to create the state if not present
     * @param consumer The consumer to modify the state
     */
    public void edit(UUID uuid, Supplier<T> factory, java.util.function.Consumer<T> consumer) {
        cache.compute(uuid, (_, state) -> {
            if (state == null)
                state = factory.get();
            consumer.accept(state);
            return state;
        });
    }

    /**
     * Clear all cached states.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Get the size of the cache.
     *
     * @return The number of cached states
     */
    public int size() {
        return cache.size();
    }

    /**
     * Get all UUIDs currently in the cache.
     *
     * @return Set of all UUIDs
     */
    public Set<UUID> keySet() {
        return cache.keySet();
    }

    /**
     * Get the underlying cache map (for advanced use cases).
     *
     * @return The cache map
     */
    public Map<UUID, T> cache() {
        return cache;
    }
}
