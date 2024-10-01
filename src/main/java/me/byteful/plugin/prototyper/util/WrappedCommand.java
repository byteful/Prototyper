package me.byteful.plugin.prototyper.util;

import me.byteful.plugin.prototyper.PrototyperPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

public class WrappedCommand extends Command implements PluginIdentifiableCommand {
    private final PrototyperPlugin plugin;
    private final Function<Object[], Object> callback;

    public WrappedCommand(PrototyperPlugin plugin, String cmd, Function<Object[], Object> callback) {
        super(cmd);
        this.plugin = plugin;
        this.callback = callback;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        callback.apply(new Object[]{sender, args});
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean unregister(CommandMap commandMap) {
        try {
            SimpleCommandMap simpleCommandMap = (SimpleCommandMap) commandMap;
            final Field knownCommands = simpleCommandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);

            final Map<String, Command> map = (Map<String, Command>) knownCommands.get(simpleCommandMap);
            map.remove(getName());
            for (String alias : getAliases()) {
                map.remove(alias);
            }
            map.remove(plugin.getName() + ":" + getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.unregister(commandMap);
    }
}
