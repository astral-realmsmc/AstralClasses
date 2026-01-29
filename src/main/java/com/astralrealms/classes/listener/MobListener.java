package com.astralrealms.classes.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.util.Constants;
import com.astralrealms.core.model.wrapper.ComponentWrapper;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class MobListener implements Listener {

    private final AstralClasses plugin;
    private final Map<TextDisplay, Long> displayTimestamps;

    public MobListener(AstralClasses plugin) {
        this.plugin = plugin;
        this.displayTimestamps = new ConcurrentHashMap<>();

        // Async cleanup task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            displayTimestamps.entrySet().removeIf(entry -> {
                if (currentTime - entry.getValue() > Constants.Display.CLEANUP_MS) {
                    TextDisplay display = entry.getKey();
                    Bukkit.getScheduler().runTask(plugin, display::remove);
                    return true;
                }
                return false;
            });
        }, 0L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        Entity victim = e.getEntity();
        if (victim instanceof Player
            || !(e.getDamageSource().getDirectEntity() instanceof Player player))
            return;

        Location eyeLoc = player.getEyeLocation();
        Vector dir = eyeLoc.getDirection().normalize();
        double yawRad = Math.toRadians(eyeLoc.getYaw());
        Vector left = new Vector(Math.cos(yawRad), 0, Math.sin(yawRad));

        Location spawnLoc = eyeLoc.clone()
                .add(dir.multiply(Constants.Display.DISTANCE))
                .add(left.multiply(Constants.Display.SIDE_OFFSET))
                .add(0, Constants.Display.HEIGHT_OFFSET, 0);

        TextDisplay display = eyeLoc.getWorld().spawn(spawnLoc, TextDisplay.class);
        ComponentWrapper component = e.getEntity().isInvulnerable() || e.getFinalDamage() == 0 ? this.plugin.configuration().damageIndicators().immune() : this.plugin.configuration().damageIndicators().damaged();
        display.text(component.component(Placeholder.unparsed("amount", String.valueOf((int) e.getFinalDamage()))));

        // Use CENTER so the text always faces the player (RPG style) without complex manual rotation math
        display.setBillboard(TextDisplay.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setShadowed(true);

        float scale = Constants.Display.TEXT_SCALE;
        display.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f()
        ));

        // Assume displayTimestamps is a field in your class
        displayTimestamps.put(display, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHandDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)
            || e.getDamageSource().getDamageType().equals(DamageType.MAGIC))
            return;

        e.setDamage(0);
    }
}
