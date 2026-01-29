package com.astralrealms.classes;

import java.util.Arrays;

import com.astralrealms.classes.command.ClassCommand;
import com.astralrealms.classes.command.ModifiersCommand;
import com.astralrealms.classes.command.context.KeyContextResolver;
import com.astralrealms.classes.configuration.MainConfiguration;
import com.astralrealms.classes.configuration.StatsConfiguration;
import com.astralrealms.classes.listener.*;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.StatModifier;
import com.astralrealms.classes.model.stat.StatType;
import com.astralrealms.classes.placeholder.StatsPlaceholderExpansion;
import com.astralrealms.classes.service.ClassService;
import com.astralrealms.classes.service.SkillService;
import com.astralrealms.classes.service.StatService;
import com.astralrealms.core.paper.plugin.AstralPaperPlugin;
import com.github.retrooper.packetevents.PacketEvents;

import lombok.Getter;
import net.kyori.adventure.key.Key;

@Getter
public final class AstralClasses extends AstralPaperPlugin {

    // Configuration
    private MainConfiguration configuration;
    private StatsConfiguration statsConfiguration;

    // Services
    private SkillService skills;
    private ClassService classes;
    private StatService stats;

    @Override
    public void onEnable() {
        super.onEnable();

        // Services (StatService must be initialized before SkillService)
        this.stats = new StatService(this);
        this.skills = new SkillService(this, this.stats);
        this.classes = new ClassService(this);

        // Configuration
        this.loadConfiguration();

        // Commands
        // -- Completion
        this.registerCompletion("statType", (_) -> Arrays.stream(StatType.values()).map(StatType::name).toList());
        this.registerCompletion("operation", (_) -> Arrays.stream(StatModifier.Operation.values()).map(Enum::name).toList());
        this.registerCompletion("inputType", (_) -> Arrays.stream(InputType.values()).map(Enum::name).toList());
        // -- Context
        this.registerContext(Key.class, new KeyContextResolver());
        // -- Command
        this.registerCommand(new ClassCommand());
        this.registerCommand(new ModifiersCommand());

        // Listeners
        PlayerCleanupListener cleanupListener = new PlayerCleanupListener();
        this.registerListeners(
                new SkillTriggerListener(this),
                new MobListener(this),
                new StatsListener(this),
                cleanupListener
        );
        PacketEvents.getAPI().getEventManager().registerListener(new InputPacketListener(this));

        // Register cleanup handlers
        this.skills.cooldownManager().clearOnQuit(cleanupListener);

        // Sync
        // AstralSyncAPI.registerAdapter(new ClassSnapshotAdapter(this));

        // API
        ClassAPI.init(this);

        // Placeholder Expansion
        new StatsPlaceholderExpansion(this).register();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void loadConfiguration() {
        // Configuration
        this.configuration = this.loadConfiguration("config.yml", MainConfiguration.class);
        this.statsConfiguration = this.loadConfiguration("stats.yml", StatsConfiguration.class);

        // Services
        this.skills.load();
        this.classes.load();
    }
}
