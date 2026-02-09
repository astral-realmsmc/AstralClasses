package com.astralrealms.classes.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.state.BasicShootState;
import com.astralrealms.classes.skill.BasicShootSkill;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

@RequiredArgsConstructor
public class ClassExpansion extends PlaceholderExpansion {

    private final AstralClasses plugin;

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] args = params.split("_");
        if (args.length == 0) return null;

        return switch (args[0]) {
            case "chargedHits" -> BasicShootSkill.getState(player)
                    .map(BasicShootState::hits)
                    .map(String::valueOf)
                    .orElse("0");
            default -> null;
        };
    }

    @Override
    public @NotNull String getIdentifier() {
        return "classes";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
}
