package com.astralrealms.classes.configuration.serializer;

import java.lang.reflect.Type;

import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.astralrealms.classes.model.InputType;

public class InputTypeSerializer implements TypeSerializer<InputType> {

    public static final InputTypeSerializer INSTANCE = new InputTypeSerializer();

    @Override
    public InputType deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String value = node.getString();
        if (value == null)
            throw new SerializationException("InputType value cannot be null");

        return InputType.valueOf(value.toUpperCase().replace("-", "_"));
    }

    @Override
    public void serialize(Type type, @Nullable InputType obj, ConfigurationNode node) throws SerializationException {

    }
}
