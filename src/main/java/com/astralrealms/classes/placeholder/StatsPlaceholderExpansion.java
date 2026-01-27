package com.astralrealms.classes.placeholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.stat.PlayerStats;
import com.astralrealms.classes.model.stat.StatType;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

@RequiredArgsConstructor
public class StatsPlaceholderExpansion extends PlaceholderExpansion {

    private final AstralClasses plugin;

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] args = params.split("_");
        if (args.length == 0)
            return null;

        PlayerStats stats = this.plugin.stats()
                .getPlayerStats(player)
                .orElse(null);
        if (stats == null)
            return null;

        return switch (args[0].toLowerCase()) {
            case "shield" -> String.valueOf(stats.getStatValue(StatType.SHIELD));
            case "max" -> {
                if (args.length < 2)
                    yield  null;

                yield switch (args[1].toLowerCase()) {
                    case "shield" -> String.valueOf(100);
                    default -> null;
                };
            }
            default -> null;
        };
    }

    @Override
    public @NotNull String getIdentifier() {
        return "stats";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", this.plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }
}
