package me.byteful.plugin.prototyper.script;

import me.byteful.plugin.prototyper.PrototyperPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class ScriptManager {
  private final Set<Script> scripts = new HashSet<>();
  private final PrototyperPlugin plugin;

  public ScriptManager(PrototyperPlugin plugin) {
    this.plugin = plugin;
  }

  public void unload() {
    scripts.forEach(Script::unload);
    scripts.clear();
  }

  public void reload(File dir) {
    unload();
    final File[] files = dir.listFiles((dir1, name) -> name.endsWith(".js"));
    if (files == null) return;

    for (File file : files) {
      try {
        final Script script = new Script(plugin, this, file.getName(), Files.readString(file.toPath()));
        script.load();
        scripts.add(script);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  void fail(Script script) {
    scripts.remove(script);
  }
}
