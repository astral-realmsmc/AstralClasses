package com.astralrealms.classes;

import java.nio.file.Path;

import org.bukkit.util.Vector;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.astralrealms.classes.command.ClassCommand;
import com.astralrealms.classes.configuration.DoubleJumpConfiguration;
import com.astralrealms.classes.configuration.serializer.VectorTypeSerializer;
import com.astralrealms.classes.listener.SkillListener;
import com.astralrealms.core.paper.configuration.serializer.PaperTypeSerializers;
import com.astralrealms.core.paper.plugin.AstralPaperPlugin;

import lombok.Getter;

@Getter
public final class AstralClasses extends AstralPaperPlugin {

    // Configuration
    private DoubleJumpConfiguration doubleJumpConfiguration;

    @Override
    public void onEnable() {
        super.onEnable();

        // Configuration
        this.loadConfiguration();

        // Commands
        this.registerCommand(new ClassCommand());

        // Listeners
        this.registerListener(new SkillListener(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void loadConfiguration() {
        Path skillsPath = this.getDataPath().resolve("skills");

        TypeSerializerCollection serializers = TypeSerializerCollection.builder()
                .register(Vector.class, new VectorTypeSerializer())
                .registerAll(PaperTypeSerializers.all())
                .build();

        // Load Double Jump Configuration
        this.doubleJumpConfiguration = this.configurationManager().load(skillsPath.resolve("double-jump.yml"), "skills/double-jump.yml", DoubleJumpConfiguration.class, c -> c.serializers(serializers));
    }
}
