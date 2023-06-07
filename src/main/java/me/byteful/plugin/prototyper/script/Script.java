package me.byteful.plugin.prototyper.script;

import me.byteful.plugin.prototyper.PrototyperPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.function.Consumer;

public class Script {
  private final Value loaded;
  private final Context context;
  private final PrototyperPlugin plugin;

  public Script(PrototyperPlugin plugin, ScriptManager manager, String name, String script) {
    this.plugin = plugin;
    this.context = Context.newBuilder().allowExperimentalOptions(true).allowHostAccess(HostAccess.ALL).option("js.nashorn-compat", "true").build();

    final Value binds = context.getBindings("js");
    binds.putMember("log", (Consumer<String>) s -> plugin.getLogger().info("[" + name + "] " + s));
    binds.putMember("Plugin", plugin);

    this.loaded = context.eval("js", script);

    if (binds.getMember("load") == null || binds.getMember("unload") == null) {
      plugin.getLogger().severe("Failed to load script: " + name);
      plugin.getLogger().severe("Please make sure you have a load() and unload() function inside the script file!");
      context.close(true);
      manager.unload(this);
    }
  }

  void load() {
    context.getBindings("js").getMember("load").executeVoid();
  }

  void unload() {
    context.getBindings("js").getMember("unload").executeVoid();
    context.close(true);
  }
}
