package com.astralrealms.classes.service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.configuration.serializer.SkillSetTypeSerializer;
import com.astralrealms.classes.model.AstralClass;
import com.astralrealms.classes.model.skill.SkillSet;
import com.astralrealms.classes.storage.ClassPlayerData;
import com.astralrealms.core.paper.configuration.serializer.PaperTypeSerializers;
import com.astralrealms.sync.paper.AstralSyncAPI;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassService {

    private final AstralClasses plugin;
    private final Map<String, AstralClass> classes = new HashMap<>();

    public void load() {
        this.plugin.getSLF4JLogger().info("Loading classes...");
        this.classes.clear();

        Path dataFolder = this.plugin.getDataFolder().toPath().resolve("classes");
        TypeSerializerCollection serializers = TypeSerializerCollection.builder()
                .register(SkillSet.class, new SkillSetTypeSerializer(this.plugin))
                .registerAll(PaperTypeSerializers.all())
                .build();

        Set<AstralClass> classes = this.plugin.configurationManager().loadFolder(dataFolder, AstralClass.class, c -> c.serializers(serializers));
        for (AstralClass astralClass : classes) {
            this.classes.put(astralClass.id(), astralClass);
        }
        this.plugin.getSLF4JLogger().info("Loaded {} classes.", this.classes.size());
    }

    public AstralClass findByPlayer(Player player) {
        return AstralSyncAPI.findData(player.getUniqueId(), ClassPlayerData.class)
                .map(data -> this.classes.get(data.selectedClassId()))
                .orElse(classes.get(this.plugin.configuration().defaultClass()));
    }
}
