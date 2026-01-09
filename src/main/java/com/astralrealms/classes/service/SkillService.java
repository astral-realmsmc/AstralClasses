package com.astralrealms.classes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.util.Vector;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.configuration.serializer.VectorTypeSerializer;
import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.skill.DoubleJumpSkill;
import com.astralrealms.core.paper.configuration.serializer.PaperTypeSerializers;
import com.astralrealms.core.registry.NamedRegistry;

public class SkillService {

    private final AstralClasses plugin;
    private final NamedRegistry<Class<? extends Skill>> skillsTypes = new NamedRegistry<>();
    private final Map<String, Skill> skills = new HashMap<>();

    public SkillService(AstralClasses plugin) {
        this.plugin = plugin;

        this.skillsTypes.register("double-jump", DoubleJumpSkill.class);
    }

    public void load() {
        this.plugin.getSLF4JLogger().info("Loading skills...");
        this.skills.clear();

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
                            this.plugin.getSLF4JLogger().warn("Unknown skill type for file: {}", path);
                            return;
                        }

                        Skill skill = this.plugin.configurationManager().load(path, skillClass, c -> c.serializers(serializers));
                        this.skills.put(strippedName, skill);
                    });
        } catch (IOException e) {
            this.plugin.getSLF4JLogger().error("Failed to load skills", e);
        }
        this.plugin.getSLF4JLogger().info("Loaded {} skills", this.skills.size());
    }
}
