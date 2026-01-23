package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.ClassAPI;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.AttackSkill;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.stat.StatType;
import com.astralrealms.classes.model.state.BasicShootState;

@ConfigSerializable
public record ChargedShootSkill(double damage, Duration cooldown) implements CooldownSkill, AttackSkill, Tickable {

    @Override
    public void trigger(Player player, InputType inputType, @Nullable SkillContext context) {
        BasicShootState state = BasicShootSkill.getState(player).orElse(null);
        if (state == null || state.hits() == 0)
            return;

        ItemDisplay grenade = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), ItemDisplay.class);
        grenade.setItemStack(new ItemStack(Material.TRIDENT));

        // 1. Set Billboards to FIXED so it doesn't rotate to face the player
        grenade.setBillboard(org.bukkit.entity.Display.Billboard.FIXED);

        float scale = 0.5f * (1.0f + 0.2f * state.hits());

        // 2. Apply a -90 degree rotation on X axis to make the vertical trident point forward
        grenade.setTransformation(new Transformation(
                new Vector3f(),                                    // Translation
                new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0), // Left Rotation (Pitch forward)
                new Vector3f(scale, scale, scale),               // Scale
                new AxisAngle4f(0, 0, 0, 0)                       // Right Rotation
        ));

        grenade.setGravity(false);

        Vector velocityVec = player.getEyeLocation().getDirection().multiply(0.7 + (0.20 * state.hits()));

        double damage = ClassAPI.getStat(player, inputType, StatType.DAMAGE, this.damage);

        // Track start location for distance calculation
        Location startLocation = grenade.getLocation().clone();
        // Keep track of already hit entities to prevent multi-ticking damage on the same enemy
        Set<UUID> hitEntities = new HashSet<>();

        state.resetHits();

        Bukkit.getScheduler().runTaskTimer(AstralClasses.getPlugin(AstralClasses.class), (task) -> {
            Location loc = grenade.getLocation();

            // 1. Check Distance Limit (25 blocks)
            if (loc.distance(startLocation) >= 25) {
                grenade.remove();
                task.cancel();
                return;
            }

            // 2. Check Block Collision (Stop if inside a solid block)
            if (loc.getBlock().getType().isSolid()) {
                grenade.remove();
                task.cancel();
                return;
            }

            // 3. Move the projectile
            loc.add(velocityVec);

            // 4. Orient the entity to face the velocity direction
            // We clone the vector to avoid modifying the actual velocity during normalization
            loc.setDirection(velocityVec.clone().normalize());

            grenade.teleport(loc);

            // 5. Apply Physics (Gravity and Air Resistance)
            velocityVec.setY(velocityVec.getY() - 0.04);
            velocityVec.multiply(0.99);

            // 6. Particle Trail
            Particle.SMOKE.builder()
                    .location(grenade.getLocation())
                    .count(2)
                    .offset(0.2, 0.2, 0.2)
                    .extra(0)
                    .spawn();

            // 7. Entity Interaction (Piercing)
            for (Entity entity : grenade.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                if (entity instanceof Player || hitEntities.contains(entity.getUniqueId())) continue;

                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.damage(damage, player);
                    player.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

                    entity.setVelocity(entity.getLocation().toVector().subtract(grenade.getLocation().toVector()).normalize().multiply(0.5).setY(0.2));
                    hitEntities.add(entity.getUniqueId());
                }
            }
        }, 0L, 1L);
    }

    @Override
    public void tick() {

    }
}
