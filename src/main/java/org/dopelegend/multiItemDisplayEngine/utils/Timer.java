package org.dopelegend.multiItemDisplayEngine.utils;


import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;

import java.util.HashMap;
import java.util.Map;

public class Timer {
    private static Map<String, Timer> timers = new HashMap<>();
    private long startTime = 0;

    public Timer(String id) {
        timers.remove(id);
        startTime = System.nanoTime();
        timers.put(id, this);
    }
    /**
     *
     * Prints the current time since this Timer was initialized in ms (with 4 decimals) to the console.
     *
     * @param message The message to print alongside the time
     * @param warning Whether this should be printed as a warning.
     */
    public void printCurrentTime(String message, boolean warning) {
        long currentTime = Math.abs(System.nanoTime()-startTime);
        double currentTimeMilliseconds = currentTime/1000000.0;
        if (warning) {
            MultiItemDisplayEngine.plugin.getLogger().warning(currentTimeMilliseconds+" ms: "+message);

        }
        MultiItemDisplayEngine.plugin.getLogger().info(currentTimeMilliseconds+" ms: "+message);
    }

    public static Timer getById(String id) {
        return timers.get(id);
    }
}
