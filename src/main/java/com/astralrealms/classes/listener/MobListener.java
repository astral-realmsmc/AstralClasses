package com.astralrealms.classes.listener;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.core.model.wrapper.ComponentWrapper;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class MobListener implements Listener {

    private final AstralClasses plugin;
    private final Map<TextDisplay, Long> displayTimestamps;

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
        if (victim instanceof Player) return; // Keep your logic: only show when hitting non-players

        Entity attacker = e.getDamager();
        if (attacker instanceof Projectile projectile) {
            attacker = (Entity) projectile.getShooter();
        }
        if (attacker == null) attacker = e.getDamager(); // Fallback for dispensers/etc

        // Use EyeLocation for the anchor point so it centers on their view
        Location eyeLoc = attacker instanceof LivingEntity ? ((LivingEntity) attacker).getEyeLocation() : attacker.getLocation();

        // 1. Calculate Forward Vector (Where the attacker is looking)
        Vector dir = eyeLoc.getDirection().normalize();

        // 2. Calculate "Left" Vector based purely on Yaw (Horizontal rotation)
        // This ensures "Left" is always to the player's left side, even if they are looking up or down.
        double yawRad = Math.toRadians(eyeLoc.getYaw());
        // In Minecraft coordinates (East is +X, South is +Z):
        // x = cos(yaw) gives the left offset, z = sin(yaw) gives the left offset
        Vector left = new Vector(Math.cos(yawRad), 0, Math.sin(yawRad));

        // 3. Define Offsets
        double distance = 2.1;   // How far in front of the face (Forward)
        double sideDist = 0.7;   // How far to the left (Left)
        double heightDist = 0.2; // How high above the eyes (Top)

        // 4. Calculate the final spawn location
        Location spawnLoc = eyeLoc.clone()
                .add(dir.multiply(distance))
                .add(left.multiply(sideDist))
                .add(0, heightDist, 0);

        TextDisplay display = eyeLoc.getWorld().spawn(spawnLoc, TextDisplay.class);
        ComponentWrapper component = e.getEntity().isInvulnerable() ? this.plugin.configuration().damageIndicators().immune() : this.plugin.configuration().damageIndicators().damaged();
        display.text(component.component(Placeholder.unparsed("amount", String.valueOf((int) e.getFinalDamage()))));

        // Use CENTER so the text always faces the player (RPG style) without complex manual rotation math
        display.setBillboard(TextDisplay.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setShadowed(true);

        float scale = 0.75f;
        display.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f()
        ));

        // Assume displayTimestamps is a field in your class
        displayTimestamps.put(display, System.currentTimeMillis());

        if (e.getEntity().isInvulnerable())
            e.setCancelled(true);
    }
}
