package com.astralrealms.classes.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.Skill;
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
            case "cooldown" -> {
                if (args.length < 2)
                    yield null;
                String skillName = args[1];
                Skill skill = this.plugin.skills()
                        .findById(skillName)
                        .orElse(null);
                if (skill == null)
                    yield "no skill";
                if (!(skill instanceof CooldownSkill cooldownSkill))
                    yield "not cooldown skill";

                if (args.length > 2) {
                    String type = args[2];
                    yield switch (type) {
                        case "max" -> String.valueOf(cooldownSkill.cooldown().getSeconds());
                        case null, default -> null;
                    };
                }

                long remaining = plugin.skills()
                        .cooldownManager()
                        .getRemainingCooldown(player, cooldownSkill);

                yield String.valueOf(Math.toIntExact(remaining / 1000));
            }
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
