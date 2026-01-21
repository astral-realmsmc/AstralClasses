package com.astralrealms.classes;

import com.astralrealms.classes.command.ClassCommand;
import com.astralrealms.classes.configuration.MainConfiguration;
import com.astralrealms.classes.configuration.StatsConfiguration;
import com.astralrealms.classes.listener.MobListener;
import com.astralrealms.classes.listener.SkillTriggerListener;
import com.astralrealms.classes.service.ClassService;
import com.astralrealms.classes.service.SkillService;
import com.astralrealms.classes.storage.ClassSnapshotAdapter;
import com.astralrealms.core.paper.plugin.AstralPaperPlugin;
import com.astralrealms.sync.paper.AstralSyncAPI;

import lombok.Getter;

@Getter
public final class AstralClasses extends AstralPaperPlugin {

    // Configuration
    private MainConfiguration configuration;
    private StatsConfiguration statsConfiguration;

    // Services
    private SkillService skills;
    private ClassService classes;

    @Override
    public void onEnable() {
        super.onEnable();

        // Services
        this.skills = new SkillService(this);
        this.classes = new ClassService(this);

        // Configuration
        this.loadConfiguration();

        // Commands
        this.registerCommand(new ClassCommand());

        // Listeners
        this.registerListeners(
                new SkillTriggerListener(this),
                new MobListener(this)
        );

        // Sync
        AstralSyncAPI.registerAdapter(new ClassSnapshotAdapter(this));
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
