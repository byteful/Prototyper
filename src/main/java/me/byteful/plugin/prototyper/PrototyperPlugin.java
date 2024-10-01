package me.byteful.plugin.prototyper;

import me.byteful.plugin.prototyper.script.ScriptManager;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.commandmanager.CommandParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class PrototyperPlugin extends JavaPlugin {
    private final ScriptManager manager = new ScriptManager(this);
    private final File scriptsDir = new File(getDataFolder(), "scripts");

    @Override
    public void onEnable() {
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
            saveTestScript();
        }

        manager.reload(scriptsDir);
        getLogger().info("Reloaded scripts!");

        new CommandParser(getResource("commands.rdcml")).parse().register(this, "prototyper", new Commands(this));
    }

    @Override
    public void onDisable() {
        manager.unload();
        getLogger().info("Unloaded scripts!");
    }

    private void saveTestScript() {
        try (InputStream in = getResource("test.js")) {
            if (in != null) {
                Files.copy(in, scriptsDir.toPath().resolve("test.js"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ScriptManager getManager() {
        return manager;
    }

    public File getScriptsDir() {
        return scriptsDir;
    }
}
