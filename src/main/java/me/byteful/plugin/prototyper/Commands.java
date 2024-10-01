package me.byteful.plugin.prototyper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import redempt.redlib.commandmanager.CommandHook;

public class Commands {
    private final PrototyperPlugin plugin;

    public Commands(PrototyperPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandHook("reload")
    public void onReload(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "[PROTOTYPER] " + ChatColor.YELLOW + "Reloading...");
        plugin.getManager().reload(plugin.getScriptsDir());
        sender.sendMessage(ChatColor.BLUE + "[PROTOTYPER] " + ChatColor.GREEN + "Successfully reloaded all scripts!");
    }

    @CommandHook("unload")
    public void onUnload(CommandSender sender, String script) {
        if (plugin.getManager().unload(script)) {
            sender.sendMessage(ChatColor.BLUE + "[PROTOTYPER] " + "Done!");
        } else {
            sender.sendMessage(ChatColor.BLUE + "[PROTOTYPER] " + ChatColor.RED + "Script not found!");
        }
    }

    @CommandHook("list")
    public void onList(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "[PROTOTYPER] " + ChatColor.GOLD + "Scripts:");
        for (String name : plugin.getManager().getScripts().keySet()) {
            sender.sendMessage(ChatColor.BLUE + "[PROTOTYPER] " + ChatColor.YELLOW + "- " + name);
        }
    }
}
