package com.astralrealms.classes.command;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.EquipmentSlot;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.stats.StatsAPI;
import com.astralrealms.stats.model.modifier.ModifierSource;
import com.astralrealms.stats.model.modifier.ModifierType;
import com.astralrealms.stats.model.modifier.impl.EnumBasedStatModifier;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

@CommandAlias("class")
@Subcommand("modifier")
@CommandPermission("class.command.modifiers")
public class ModifierCommand extends BaseCommand {

    @Dependency
    private AstralClasses plugin;

    @Default
    @Syntax("<player> <key> <stat> <value> <modifierType> <inputType>")
    public void onDefault(CommandSender sender, OnlinePlayer target, Key key, Key statKey, double value, ModifierType modifierType, InputType inputType) {
        StatsAPI.addModifier(target.player, statKey, new EnumBasedStatModifier<>(key, statKey, ModifierSource.OTHER, EquipmentSlot.SADDLE, value, modifierType, inputType));
        sender.sendMessage(Component.text("Added modifier to " + target.player.getName() + ": " + key + " -> " + statKey + " = " + value + " (" + modifierType + ", " + inputType + ")"));
    }

}
