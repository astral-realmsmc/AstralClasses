package com.astralrealms.classes.configuration.serializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.skill.Skill;
import com.astralrealms.classes.model.skill.SkillSet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillSetTypeSerializer implements TypeSerializer<SkillSet> {

    private final AstralClasses plugin;

    @Override
    public SkillSet deserialize(@NonNull Type type, @NonNull ConfigurationNode node) {
        Map<InputType, Skill> skills = new HashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            InputType inputType = InputType.valueOf(entry.getKey().toString().toUpperCase().replace("-", "_"));
            String skillId = entry.getValue().getString();
            if (skillId == null)
                throw new IllegalStateException("Skill ID cannot be null for input type: " + inputType);
            Skill skill = this.plugin.skills()
                    .findById(skillId)
                    .orElseThrow(() -> new IllegalStateException("Skill with ID '" + skillId + "' not found for input type: " + inputType));
            skills.put(inputType, skill);
        }
        return new SkillSet(skills);
    }

    @Override
    public void serialize(@NonNull Type type, @Nullable SkillSet obj, @NonNull ConfigurationNode node) {
        throw new UnsupportedOperationException("Serialization of SkillSet is not supported.");
    }
}
