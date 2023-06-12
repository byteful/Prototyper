package me.byteful.plugin.prototyper.script;

import me.byteful.plugin.prototyper.PrototyperPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.function.Consumer;

public class Script {
  private final Context context;

  public Script(PrototyperPlugin plugin, ScriptManager manager, String name, String script) {
    this.context = Context.newBuilder().allowExperimentalOptions(true).allowHostAccess(HostAccess.ALL).option("js.nashorn-compat", "true").build();

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

  void load() {
    context.getBindings("js").getMember("load").executeVoid();
  }

  void unload() {
    context.getBindings("js").getMember("unload").executeVoid();
    context.close(true);
  }

  private static void init(PrototyperPlugin plugin, String name, Value binds) {
    //
    // -- Functions
    //

    // log(Object)
    binds.putMember("log", (Consumer<Object>) s -> plugin.getLogger().info("[" + name + "] " + s));
    // registerCommand(String, BiConsumer<CommandSender, String[]>)

    // registerListener(Class<? extends Event>, Consumer<Event>)

    //
    // -- Variables
    //

    binds.putMember("Plugin", plugin);
    binds.putMember("Bukkit", plugin.getServer());
  }
}
