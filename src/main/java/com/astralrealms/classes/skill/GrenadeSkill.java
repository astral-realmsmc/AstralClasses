package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.util.Effects;
import com.astralrealms.classes.util.GameUtils;
import com.destroystokyo.paper.ParticleBuilder;

@ConfigSerializable
public record GrenadeSkill(ItemStack item, double velocity, double impactRange, double damage,
                           double knockbackVelocity, Duration cooldown) implements AttackSkill, CooldownSkill {

    @Override
    public void trigger(Player player, InputType inputType, SkillContext context) {
        Item grenade = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Item.class);
        grenade.setItemStack(item);

        // Launch the grenade forward with some upward velocity
        grenade.setVelocity(player.getEyeLocation().getDirection().multiply(velocity));

        // Compute damage
        double damage = GameUtils.computeDamage(player, inputType, this.damage);

        // When the grenade lands, create an explosion effect and remove the grenade
        AtomicReference<Location> lastLocation = new AtomicReference<>(grenade.getLocation());
        long launchTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskTimer(AstralClasses.getPlugin(AstralClasses.class), (task) -> {
            if (!grenade.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid() &&
                System.currentTimeMillis() - launchTime < 3000) {
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
            Effects.explosion(lastLocation.get());

            // Remove the grenade item
            grenade.remove();

            // Apply knockback and damage to nearby players
            List<Entity> nearbyEntities = grenade.getWorld()
                    .getNearbyEntities(grenade.getLocation(), impactRange, impactRange, impactRange)
                    .stream()
                    .toList();
            for (Entity entity : nearbyEntities) {
                entity.setVelocity(entity.getLocation().toVector().subtract(grenade.getLocation().toVector()).normalize().multiply(this.knockbackVelocity).setY(0.5));
                if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player))  // Prevent friendly fire
                    livingEntity.damage(damage, DamageSource.builder(DamageType.MAGIC)
                            .withDirectEntity(player)
                            .build());
            }

            // Play sound
            Effects.playExplosionSound(lastLocation.get());

            task.cancel();
        }, 0L, 1L);
    }

}
