package com.astralrealms.classes.configuration.serializer;

import java.lang.reflect.Type;

import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class VectorTypeSerializer implements TypeSerializer<Vector> {

    @Override
    public Vector deserialize(@NonNull Type type, @NonNull ConfigurationNode node) {
        double x = node.node("x").getDouble();
        double y = node.node("y").getDouble();
        double z = node.node("z").getDouble();
        return new Vector(x, y, z);
    }

    @Override
    public void serialize(@NonNull Type type, @Nullable Vector obj, @NonNull ConfigurationNode node) {

    }
}
