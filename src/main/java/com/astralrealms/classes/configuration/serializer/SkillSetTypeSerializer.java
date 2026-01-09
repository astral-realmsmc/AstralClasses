package com.astralrealms.classes.configuration.serializer;

import java.lang.reflect.Type;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.skill.SkillSet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillSetTypeSerializer implements TypeSerializer<SkillSet> {

    private final AstralClasses plugin;

    @Override
    public SkillSet deserialize(@NonNull Type type, @NonNull ConfigurationNode node) throws SerializationException {

        return null;
    }

    @Override
    public void serialize(@NonNull Type type, @Nullable SkillSet obj, @NonNull ConfigurationNode node) throws SerializationException {

    }
}
