package com.astralrealms.classes.visual;

import com.astralrealms.classes.AstralClasses;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class GeneralParticle {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static void spawnHitParticleEffect(Location location) {

        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(AstralClasses.instance(), () -> spawnHitParticleEffect(location));
        }

        // Create a sharp impact effect - particles radiating outward from hit point
        final int particleCount = 15;

        // Directional burst - particles flying away from the impact
        for (int i = 0; i < particleCount; i++) {
            // Random direction in a sphere
            double yaw = RANDOM.nextDouble() * 2 * Math.PI;
            double pitch = (RANDOM.nextDouble() - 0.5) * Math.PI;

            double x = Math.cos(yaw) * Math.cos(pitch);
            double y = Math.sin(pitch);
            double z = Math.sin(yaw) * Math.cos(pitch);

            Vector direction = new Vector(x, y, z).normalize().multiply(0.5);
            Location particleLoc = location.clone().add(0, 1, 0);

            // Use CRIT particles for a sharp hit feel
            Particle.CRIT.builder()
                    .location(particleLoc)
                    .count(1)
                    .offset(x * 0.5, y * 0.5, z * 0.5)
                    .extra(0.1)
                    .spawn();
        }

        // Add a white flash at the center
        for (int i = 0; i < 5; i++) {
            Location flashLoc = location.clone().add(
                    (RANDOM.nextDouble() - 0.5) * 0.3,
                    1 + (RANDOM.nextDouble() - 0.5) * 0.3,
                    (RANDOM.nextDouble() - 0.5) * 0.3
            );

            new ParticleBuilder(Particle.DUST)
                    .location(flashLoc)
                    .count(1)
                    .offset(0.1, 0.1, 0.1)
                    .extra(1)
                    .color(Color.RED)
                    .spawn();
        }

        // Add a few sparkles for extra impact feel
        for (int i = 0; i < 3; i++) {
            Location sparkleLoc = location.clone().add(0, 1, 0);

            Particle.ITEM.builder()
                    .data(new ItemStack(Material.REDSTONE_BLOCK))
                    .location(sparkleLoc)
                    .count(2)
                    .offset(0.3, 0.3, 0.3)
                    .extra(0.5)
                    .spawn();
        }
    }
}
