package com.astralrealms.classes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.configuration.serializer.VectorTypeSerializer;
import com.astralrealms.classes.model.AstralClass;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.skill.*;
import com.astralrealms.core.paper.configuration.serializer.PaperTypeSerializers;
import com.astralrealms.core.registry.NamedRegistry;

public class SkillService {

    private final AstralClasses plugin;
    private final NamedRegistry<Class<? extends Skill>> skillsTypes = new NamedRegistry<>();
    private final Map<String, Skill> skills = new HashMap<>();
    private final CooldownManager cooldownManager;
    final SkillTriggerManager triggerManager;

    public SkillService(AstralClasses plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager();
        this.triggerManager = new SkillTriggerManager(plugin, 1);

        this.skillsTypes.register("double-jump", DoubleJumpSkill.class);
        this.skillsTypes.register("basic-projectile", BasicShootSkill.class);
        this.skillsTypes.register("advanced-projectile", AdvancedShootSkill.class);
        this.skillsTypes.register("grenade", GrenadeSkill.class);
        this.skillsTypes.register("stat-modifier", StatModifierSkill.class);

        // Start ticking task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Skill skill : this.skills.values()) {
                if (skill instanceof Tickable tickable)
                    tickable.tick();
            }
        }, 20L, 10L);
    }

    public void load() {
        this.plugin.getSLF4JLogger().info("Loading skills...");
        this.skills.clear();

        // Start trigger manager tick counter
        this.triggerManager.startTickCounter();

        TypeSerializerCollection serializers = TypeSerializerCollection.builder()
                .register(Vector.class, new VectorTypeSerializer())
                .registerAll(PaperTypeSerializers.all())
                .build();

        Path dataFolder = this.plugin.getDataFolder().toPath().resolve("skills");
        try (Stream<Path> pathStream = Files.walk(dataFolder)) {
            pathStream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .forEach(path -> {
                        String strippedName = dataFolder.relativize(path)
                                .toString()
                                .replace(".yml", "");

                        Class<? extends Skill> skillClass = this.skillsTypes.findByName(strippedName)
                                .orElse(null);
                        if (skillClass == null) {
                            this.plugin.getSLF4JLogger().warn("Unknown skill operation for file: {}", path);
                            return;
                        }

                        Skill skill = this.plugin.configurationManager().load(path, skillClass, c -> c.serializers(serializers));
                        if (skill == null) {
                            this.plugin.getSLF4JLogger().warn("Failed to load skill from file: {}", path);
                            return;
                        }
                        this.skills.put(strippedName, skill);

                        // Register listener if it's one
                        if (skill instanceof Listener listener)
                            this.plugin.registerListener(listener);
                    });
        } catch (IOException e) {
            this.plugin.getSLF4JLogger().error("Failed to load skills", e);
        }
        this.plugin.getSLF4JLogger().info("Loaded {} skills", this.skills.size());
    }

    public void tryTriggerSkill(Player player, InputType type, Supplier<@Nullable SkillContext> contextSupplier) {
        AstralClass astralClass = this.plugin.classes().findByPlayer(player);
        if (astralClass != null)
            this.tryTriggerSkill(player, astralClass, type, contextSupplier);
    }

    public void tryTriggerSkill(Player player, AstralClass astralClass, InputType type, Supplier<@Nullable SkillContext> contextSupplier) {
        astralClass.findSkillByInput(type)
                .ifPresent(skill -> {
                    // Check if player can trigger a skill this tick (before cooldowns)
                    if (!triggerManager.canTrigger(player))
                        return;

                    // Handle cooldowns
                    if (skill instanceof CooldownSkill cooldownSkill) {
                        if (!cooldownManager.canUse(player, cooldownSkill, type))
                            return;
                        cooldownManager.recordUsage(player, skill);
                    }

                    // Record the trigger after all checks pass
                    triggerManager.recordTrigger(player);

                    Runnable run = () -> skill.trigger(player, type, contextSupplier.get());

                    if (Bukkit.isPrimaryThread())
                        run.run();
                    else
                        Bukkit.getScheduler().runTask(this.plugin, run);
                });
    }

    public Optional<Skill> findById(String id) {
        return Optional.ofNullable(this.skills.get(id));
    }

    public CooldownManager cooldownManager() {
        return cooldownManager;
    }

    public SkillTriggerManager triggerManager() {
        return triggerManager;
    }
}
