package com.ecommerce.util;

import javafx.application.Platform;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * DataEventBus is a simple observer pattern utility to synchronize UI components.
 * When data changes in one part of the app, it can notify all registered listeners.
 */
public class DataEventBus {
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    /**
     * Subscribe a listener to data change events.
     * @param listener The runnable to execute when data changes.
     */
    public static void subscribe(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Notify all listeners that data has changed.
     * Updates are always pushed to the JavaFX Application Thread.
     */
    public static void publish() {
        for (Runnable listener : listeners) {
            Platform.runLater(listener);
        }
    }
    
    /**
     * Clear all current listeners (useful on logout/session end).
     */
    public static void clear() {
        listeners.clear();
    }
}
