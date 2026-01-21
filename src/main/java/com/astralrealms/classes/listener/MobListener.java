package com.astralrealms.classes.listener;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import com.astralrealms.classes.AstralClasses;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MobListener implements Listener {

    private final AstralClasses plugin;
    private final Map<TextDisplay, Long> displayTimestamps;

    // Configuration for distance-based scaling
    private static final double MIN_DISTANCE = 3.0;  // Distance where scale is smallest
    private static final double MAX_DISTANCE = 25.0; // Distance where scale is largest
    private static final float MIN_SCALE = 0.8f;     // Minimum scale multiplier
    private static final float MAX_SCALE = 2.5f;     // Maximum scale multiplier

    public MobListener(AstralClasses plugin) {
        this.plugin = plugin;
        this.displayTimestamps = new java.util.concurrent.ConcurrentHashMap<>();

        // Async cleanup task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            displayTimestamps.entrySet().removeIf(entry -> {
                if (currentTime - entry.getValue() > 600) {
                    TextDisplay display = entry.getKey();
                    Bukkit.getScheduler().runTask(plugin, display::remove);
                    return true;
                }
                return false;
            });
        }, 0L, 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        if (victim instanceof Player) return;

        Entity attacker = e.getDamager();
        if (attacker instanceof Projectile) {
            attacker = (Entity) ((Projectile) attacker).getShooter();
        }
        if (attacker == null) attacker = e.getDamager();

        Location spawnLoc = victim.getLocation().add(0, victim.getHeight() + 0.5, 0);
        Location attackerLoc = attacker.getLocation();

        // Calculate distance between attacker and victim
        double distance = attackerLoc.distance(spawnLoc);

        // Calculate scale based on distance (linear interpolation)
        float scale = calculateScale(distance);

        TextDisplay display = victim.getWorld().spawn(spawnLoc, TextDisplay.class);

        display.text(Component.text("-" + (int) e.getFinalDamage(), NamedTextColor.RED, TextDecoration.BOLD));
        display.setBillboard(TextDisplay.Billboard.FIXED);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setShadowed(true);

        // Calculate direction from victim to attacker
        Vector direction = attackerLoc.toVector().subtract(spawnLoc.toVector()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));

        display.setRotation(yaw, 0);
        display.setTransformation(new Transformation(
                new Vector3f(),
                new org.joml.AxisAngle4f(),
                new Vector3f(scale, scale, scale),
                new org.joml.AxisAngle4f()
        ));

        displayTimestamps.put(display, System.currentTimeMillis());
    }

    /**
     * Calculates the scale factor based on distance.
     * Farther distances result in larger scales for better visibility.
     *
     * @param distance The distance between attacker and victim
     * @return The calculated scale factor
     */
    private float calculateScale(double distance) {
        // Clamp distance to our defined range
        distance = Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, distance));

        // Linear interpolation between MIN_SCALE and MAX_SCALE
        double ratio = (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE);

        return (float) (MIN_SCALE + (MAX_SCALE - MIN_SCALE) * ratio);
    }
}
