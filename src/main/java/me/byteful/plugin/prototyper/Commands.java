package me.byteful.plugin.prototyper;

import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.help.Help;

import static org.bukkit.ChatColor.*;

@Command("prototyper")
@CommandPermission("prototyper.admin")
public class Commands {
    public static final String PREFIX = BLUE + "[PROTOTYPER] ";
    @Dependency
    private PrototyperPlugin plugin;

    @Subcommand("help")
    public void onHelp(CommandSender sender, Help.SiblingCommands<BukkitCommandActor> commands) {
        sender.sendMessage(PREFIX + GOLD + "List of commands:");
        commands.all()
                .stream()
                .map(command -> "- " + command.usage())
                .forEach(sender::sendMessage);
    }

    @Subcommand("reload")
    public void onReload(CommandSender sender) {
        sender.sendMessage(PREFIX + YELLOW + "Reloading...");
        plugin.getManager().reload(plugin.getScriptsDir());
        sender.sendMessage(PREFIX + GREEN + "Successfully reloaded all scripts!");
    }

    @Subcommand("unload")
    public void onUnload(CommandSender sender, String script) {
        if (plugin.getManager().unload(script)) {
            sender.sendMessage(PREFIX + "Done!");
        } else {
            sender.sendMessage(PREFIX + RED + "Script not found!");
        }
    }

    @Subcommand("list")
    public void onList(CommandSender sender) {
        sender.sendMessage(PREFIX + GOLD + "Scripts:");
        for (String name : plugin.getManager().getScripts().keySet()) {
            sender.sendMessage(PREFIX + YELLOW + "- " + name);
        }
    }
}
