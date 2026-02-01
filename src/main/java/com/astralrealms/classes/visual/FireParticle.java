package com.astralrealms.classes.visual;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.util.GameUtils;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class FireParticle {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static void spawnFireTrail(Location eyeLocation, Vector direction, double maxDistance) {

        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(AstralClasses.instance(), () -> spawnFireTrail(eyeLocation, direction, maxDistance));
        }

        Vector flameOffset = new Vector(0.2, 0.2, 0.2);

        ParticleBuilder ashParticle = Particle.ASH.builder()
                .offset(flameOffset.getX()-0.1, flameOffset.getY()-0.1, flameOffset.getZ()-0.1)
                .count(4)
                .extra(0f);
        ParticleBuilder flameRedParticle = Particle.DUST.builder()
                .color(250,35,13)
                .offset(flameOffset.getX(), flameOffset.getY(), flameOffset.getZ())
                .extra(0f);
        ParticleBuilder flameRedOrangeParticle = Particle.DUST_COLOR_TRANSITION.builder()
                .colorTransition(226,56,34,226,120,34)
                .offset(flameOffset.getX(), flameOffset.getY(), flameOffset.getZ())
                .extra(0f);
        ParticleBuilder crimsonParticle = Particle.CRIMSON_SPORE.builder()
                .offset(flameOffset.getX(), flameOffset.getY(), flameOffset.getZ())
                .extra(0.1f);
        ParticleBuilder flameParticle = Particle.SMALL_FLAME.builder()
                .offset(flameOffset.getX()-0.1, flameOffset.getY()-0.1, flameOffset.getZ()-0.1)
                .extra(0.01f);

        // Spawn particles up to the hit point
        for (double t = 0; t < maxDistance; t += 0.3) {
            Location baseLocation = eyeLocation.clone().add(direction.clone().multiply(t));

            ashParticle.location(baseLocation).spawn();
            flameRedParticle.count(Math.toIntExact((long) Math.ceil(RANDOM.nextFloat()*2))).location(baseLocation).spawn();
            flameRedOrangeParticle.count(Math.toIntExact((long) Math.ceil(RANDOM.nextFloat()*2))).location(baseLocation).spawn();
            if(t > 1.5 && t - Math.abs(t) < 0.2){
                crimsonParticle.count(1).location(baseLocation).spawn();
            }
            flameParticle.count(1).location(baseLocation).spawn();
        }
    }

    public static void spawnFireHelixEffect(Location eyeLocation, Vector direction, Vector perpendicular, double helixRadius, double maxDistance) {

        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(AstralClasses.instance(), () -> spawnFireHelixEffect(eyeLocation, direction, perpendicular, helixRadius, maxDistance));
        }

        Vector flameOffset = new Vector(0.05, 0.05, 0.05);

        ParticleBuilder trailParticle = Particle.TRAIL.builder()
                .count(3)
                .offset(flameOffset.getX(), flameOffset.getY(), flameOffset.getZ())
                .extra(0f);

        ParticleBuilder flameParticle = Particle.SMALL_FLAME.builder()
                .count(1)
                .offset(0,0,0)
                .extra(0f);

        // Spawn particles up to the hit point
        for (double t = 0; t < maxDistance; t += 0.3) {
            Location baseLocation = eyeLocation.clone().add(direction.clone().multiply(t));
            Location nextLocation = eyeLocation.clone().add(direction.clone().multiply(t+0.6));

            flameParticle.location(baseLocation).spawn();

            double angle = t * 2;

            // Calculate perpendicular offsets for the helix
            Vector rotatedPerp = GameUtils.rotateAroundAxis(perpendicular, direction, angle);
            Vector offset1 = rotatedPerp.clone().multiply(helixRadius);
            Vector offset2 = rotatedPerp.clone().multiply(-helixRadius);

            trailParticle.location(baseLocation.clone().add(offset1)).data(new Particle.Trail(nextLocation.clone().add(offset2), Color.fromRGB(255, Math.round(RANDOM.nextFloat()*255), 0), 10)).spawn();
            trailParticle.location(baseLocation.clone().add(offset2)).data(new Particle.Trail(nextLocation.clone().add(offset1), Color.fromRGB(255, Math.round(RANDOM.nextFloat()*255), 0), 10)).spawn();

        }
    }
}
