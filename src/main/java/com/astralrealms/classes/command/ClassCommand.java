package com.astralrealms.classes.command;

import org.bukkit.command.CommandSender;

import com.astralrealms.classes.AstralClasses;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandAlias("class")
@Description("Base command for class-related actions.")
@CommandPermission("class.command")
public class ClassCommand extends BaseCommand {

    @Dependency
    private AstralClasses plugin;

    @Subcommand("reload")
    @Description("Reloads the class configurations.")
    public void onReloadCommand(CommandSender issuer) {
        issuer.sendMessage(Component.text("Reloading class configurations...", NamedTextColor.GRAY));
        try {
            plugin.loadConfiguration();
            issuer.sendMessage(Component.text("Class configurations reloaded successfully.", NamedTextColor.GREEN));
        } catch (Exception e) {
            issuer.sendMessage(Component.text("An error occurred while reloading configurations.", NamedTextColor.RED));
            this.plugin.getSLF4JLogger().error("Error reloading class configurations", e);
        }
    }
}
