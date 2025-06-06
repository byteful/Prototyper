package me.byteful.plugin.prototyper.script;

import me.byteful.plugin.prototyper.PrototyperPlugin;
import me.byteful.plugin.prototyper.util.WrappedCommand;
import me.byteful.plugin.prototyper.util.WrappedEventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Script {
    private final Context context;
    private final PrototyperPlugin plugin;
    private final List<WrappedEventListener> eventListeners = new ArrayList<>();
    private final Map<String, WrappedCommand> registeredCommands = new HashMap<>();

    public Script(PrototyperPlugin plugin, ScriptManager manager, String name, String script) {
        this.plugin = plugin;

        final Engine engine = Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build();
        this.context = Context.newBuilder().allowExperimentalOptions(true).allowHostAccess(HostAccess.ALL).allowAllAccess(true).currentWorkingDirectory(plugin.getScriptsDir().toPath().toAbsolutePath()).engine(engine).build();

        final Value binds = context.getBindings("js");
        init(plugin, name, binds);

        context.eval("js", script);

        if (binds.getMember("load") == null || binds.getMember("unload") == null) {
            plugin.getLogger().severe("Failed to load script: " + name);
            plugin.getLogger().severe("Please make sure you have a load() and unload() function inside the script file!");
            context.close(true);
            manager.fail(this);
        }
    }

    private void init(PrototyperPlugin plugin, String name, Value binds) {
        //
        // -- Functions
        //

        // log(Object)
        binds.putMember("log", (Consumer<Object>) s -> plugin.getLogger().info("[" + name + "] " + s));
        // registerCommand(String, BiConsumer<CommandSender, String[]>)
        binds.putMember("registerCommand", (BiConsumer<String, Function<Object[], Object>>) (cmd, callback) -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    final CommandMap commandMap = (CommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
                    final WrappedCommand command = new WrappedCommand(plugin, cmd, callback);

                    commandMap.register(plugin.getName(), command);
                    registeredCommands.put(cmd, command); // Track registered commands
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to register command: " + cmd);
                    e.printStackTrace();
                }
            });

            plugin.getLogger().info("Registered command: " + cmd);
        });
        // registerListener(Class<? extends Event>, Consumer<Event>)
        binds.putMember("registerListener", (BiConsumer<Class<? extends Event>, Function<Object[], Object>>) (s, e) -> {
            final WrappedEventListener listener = new WrappedEventListener(s, (x) -> e.apply(new Object[]{x}));
            eventListeners.add(listener);
            Bukkit.getPluginManager().registerEvent(s, listener, EventPriority.NORMAL, listener, plugin, false);
        });

        //
        // -- Variables
        //

        binds.putMember("Plugin", plugin);
        binds.putMember("Bukkit", plugin.getServer());
    }

    void load() {
        context.getBindings("js").getMember("load").executeVoid();
    }

    void unload() {
        final Value binds = context.getBindings("js");
        binds.getMember("unload").executeVoid();
        binds.removeMember("registerCommand");
        binds.removeMember("registerListener");
        binds.removeMember("Plugin");
        binds.removeMember("Bukkit");
        for (WrappedEventListener listener : eventListeners) {
            HandlerList.unregisterAll(listener);
        }
        try {
            final CommandMap commandMap = (CommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());

            for (String cmd : registeredCommands.keySet()) {
                commandMap.getCommand(cmd).unregister(commandMap);
                plugin.getLogger().info("Unregistered command: " + cmd);
            }

            registeredCommands.clear();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to unregister commands.");
            e.printStackTrace();
        }
        context.close(true);
    }
}
