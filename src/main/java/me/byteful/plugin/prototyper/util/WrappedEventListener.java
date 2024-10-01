package me.byteful.plugin.prototyper.util;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.function.Consumer;

public class WrappedEventListener implements Listener, EventExecutor {
    private final Class<? extends Event> type;
    private final Consumer<Event> callback;

    public WrappedEventListener(Class<? extends Event> type, Consumer<Event> callback) {
        this.type = type;
        this.callback = callback;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (event.getClass().equals(type)) {
            callback.accept(event);
        }
    }
}
