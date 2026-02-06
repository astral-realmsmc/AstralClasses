package com.astralrealms.classes;

import java.util.Arrays;

import com.astralrealms.classes.command.ClassCommand;
import com.astralrealms.classes.command.ModifierCommand;
import com.astralrealms.classes.command.context.KeyContextResolver;
import com.astralrealms.classes.configuration.MainConfiguration;
import com.astralrealms.classes.listener.*;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.service.ClassService;
import com.astralrealms.classes.service.SkillService;
import com.astralrealms.core.paper.plugin.AstralPaperPlugin;
import com.github.retrooper.packetevents.PacketEvents;

import lombok.Getter;
import net.kyori.adventure.key.Key;

@Getter
public final class AstralClasses extends AstralPaperPlugin {

    @Getter
    private static AstralClasses instance;

    // Configuration
    private MainConfiguration configuration;

    // Services
    private SkillService skills;
    private ClassService classes;

    @Override
    public void onEnable() {
        super.onEnable();

        instance = this;

        // Services
        this.skills = new SkillService(this);
        this.classes = new ClassService(this);

        // Configuration
        this.loadConfiguration();

        // Commands
        // -- Completion
        this.registerCompletion("inputType", (_) -> Arrays.stream(InputType.values()).map(Enum::name).toList());
        // -- Context
        this.registerContext(Key.class, new KeyContextResolver());
        // -- Command
        this.registerCommand(new ClassCommand());
        this.registerCommand(new ModifierCommand());

        // Listeners
        PlayerCleanupListener cleanupListener = new PlayerCleanupListener();
        this.registerListeners(
                new SkillTriggerListener(this),
                new MobListener(this),
                cleanupListener
        );
        PacketEvents.getAPI().getEventManager().registerListener(new InputPacketListener(this));

        // Register cleanup handlers
        this.skills.cooldownManager().clearOnQuit(cleanupListener);

        // Sync
        // AstralSyncAPI.registerAdapter(new ClassSnapshotAdapter(this));

        // API
        ClassAPI.init(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void loadConfiguration() {
        // Configuration
        this.configuration = this.loadConfiguration("config.yml", MainConfiguration.class);

        // Services
        this.skills.load();
        this.classes.load();
    }
}
