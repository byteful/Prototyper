package me.byteful.plugin.prototyper.script;

import me.byteful.plugin.prototyper.PrototyperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ScriptManager {
    private final Map<String, Script> scripts = new HashMap<>();
    private final PrototyperPlugin plugin;

    public ScriptManager(PrototyperPlugin plugin) {
        this.plugin = plugin;
    }

    private void reloadPlayerCommands() {
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    public void unload() {
        boolean alreadyEmpty = scripts.isEmpty();
        scripts.values().forEach(Script::unload);
        scripts.clear();
        if (!alreadyEmpty) {
            reloadPlayerCommands();
        }
    }

    public boolean unload(String scriptName) {
        final Script script = scripts.remove(scriptName);
        if (script != null) {
            script.unload();
            reloadPlayerCommands();
            return true;
        }

        return false;
    }

    public Map<String, Script> getScripts() {
        return scripts;
    }

    public void reload(File dir) {
        unload();
        final File[] files = dir.listFiles((dir1, name) -> name.endsWith(".js"));
        if (files == null) return;

        for (File file : files) {
            try {
                final Script script = new Script(plugin, this, file.getName(), Files.readString(file.toPath()));
                script.load();
                scripts.put(file.getName(), script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Delay so scripts get some time to initialize and no commands are left out.
        Bukkit.getScheduler().runTaskLater(plugin, this::reloadPlayerCommands, 20L);
    }

    void fail(Script script) {
        scripts.remove(script);
    }
}
