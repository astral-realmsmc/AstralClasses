package com.astralrealms.classes.command.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import net.kyori.adventure.key.Key;

public class KeyContextResolver implements ContextResolver<Key, BukkitCommandExecutionContext> {

    @Override
    public Key getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String input = context.popFirstArg();
        try {
            return Key.key(input);
        } catch (Exception e) {
            throw new InvalidCommandArgument("Invalid key: " + input);
        }
    }
}
