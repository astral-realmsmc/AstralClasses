package com.astralrealms.classes.skill;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.destroystokyo.paper.ParticleBuilder;

@ConfigSerializable
public record GrenadeSkill(ItemStack item, double velocity, double impactRange, double damage,
                           double knockbackVelocity) implements Skill {

    @Override
    public void trigger(Player player, SkillContext context) {
        Item grenade = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Item.class);
        grenade.setItemStack(item);

        // Launch the grenade forward with some upward velocity
        grenade.setVelocity(player.getEyeLocation().getDirection().multiply(velocity));

        // When the grenade lands, create an explosion effect and remove the grenade
        AtomicReference<Location> lastLocation = new AtomicReference<>(grenade.getLocation());
        Bukkit.getScheduler().runTaskTimer(AstralClasses.getPlugin(AstralClasses.class), (task) -> {
            if (!grenade.isOnGround()) {
                ParticleBuilder smokeParticle = Particle.SMOKE.builder()
                        .location(grenade.getLocation())
                        .count(5)
                        .offset(0.2, 0.2, 0.2)
                        .extra(0);
                smokeParticle.spawn();

                lastLocation.set(grenade.getLocation().clone());
                return;
            }

            // Create explosion effect
            Particle.EXPLOSION.builder()
                    .location(lastLocation.get())
                    .count(7)
                    .offset(0.5, 0.5, 0.5)
                    .extra(0)
                    .spawn();

            // Remove the grenade item
            grenade.remove();

            // Apply knockback and damage to nearby players
            List<Entity> nearbyEntities = grenade.getWorld().getNearbyEntities(grenade.getLocation(), impactRange, impactRange, impactRange).stream()
                    .toList();
            for (Entity entity : nearbyEntities) {
                entity.setVelocity(entity.getLocation().toVector().subtract(grenade.getLocation().toVector()).normalize().multiply(this.knockbackVelocity).setY(0.5));
                if (entity instanceof LivingEntity livingEntity)
                    livingEntity.damage(damage, player);
            }

            task.cancel();
        }, 0L, 1L);
    }

}
