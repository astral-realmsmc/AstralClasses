package com.astralrealms.classes.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.StatModifier;
import com.astralrealms.classes.model.stat.StatType;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.kyori.adventure.key.Key;

@CommandAlias("class")
@CommandPermission("class.command")
@Subcommand("modifiers")
public class ModifiersCommand extends BaseCommand {

    @Dependency
    private AstralClasses plugin;

    @Subcommand("list")
    @CommandPermission("class.command.modifiers.list")
    public void onList(Player player) {
        this.plugin.stats()
                .getPlayerStats(player)
                .ifPresentOrElse(playerStats -> {
                    player.sendMessage("Input Modifiers:");
                    playerStats.inputModifiers().forEach((inputType, statModifiers) -> {
                        player.sendMessage("  " + inputType.name() + ":");
                        statModifiers.forEach(modifier -> player.sendMessage("    - " + formatModifier(modifier)));
                    });

                    player.sendMessage("Global Modifiers:");
                    playerStats.globalModifiers().forEach(modifier -> player.sendMessage("  - " + formatModifier(modifier)));
                }, () -> player.sendMessage("No stats found for you."));
    }

    private String formatModifier(StatModifier modifier) {
        return modifier.type() + " | " + modifier.key() + ": " + (modifier.operation().equals(StatModifier.Operation.MULTIPLICATIVE) ? "x" : "+") + " " + modifier.value();
    }

    @Subcommand("addGlobal")
    @CommandPermission("class.command.modifiers.add")
    @Syntax("<key> <statType> <value> <operation>")
    @CommandCompletion("@players @nothing @statType @nothing @operation")
    public void onAdd(CommandSender sender, OnlinePlayer player, Key key, StatType type, double value, StatModifier.Operation operation) {
        StatModifier modifier = new StatModifier(key, type, operation, value, true);
        this.plugin.stats()
                .getPlayerStats(player.player)
                .ifPresentOrElse(playerStats -> {
                    playerStats.addGlobalModifier(modifier);
                    sender.sendMessage("Added global modifier: " + key + " with value " + value + " and operation " + operation);
                }, () -> sender.sendMessage("No stats found for you."));
    }

    @Subcommand("addInput")
    @CommandPermission("class.command.modifiers.add")
    @Syntax("<inputType> <key> <statType> <value> <operation>")
    @CommandCompletion("@players @inputType @nothing @statType @nothing @operation")
    public void onAddInput(CommandSender sender, OnlinePlayer player, InputType inputType, Key key, StatType type, double value, StatModifier.Operation operation) {
        StatModifier modifier = new StatModifier(key, type, operation, value, false);
        this.plugin.stats()
                .getPlayerStats(player.player)
                .ifPresentOrElse(playerStats -> {
                    playerStats.addInputModifier(inputType, modifier);
                    sender.sendMessage("Added input modifier: " + key + " with value " + value + " and operation " + operation + " for type " + type.name());
                }, () -> sender.sendMessage("No stats found for you."));
    }

    @Subcommand("remove")
    @CommandPermission("class.command.modifiers.remove")
    public void onRemove(Player player, Key key) {
        this.plugin.stats()
                .getPlayerStats(player)
                .ifPresentOrElse(playerStats -> {
                    playerStats.removeModifier(key);
                    player.sendMessage("Removed modifier: " + key);
                }, () -> player.sendMessage("No stats found for you."));
    }

}
