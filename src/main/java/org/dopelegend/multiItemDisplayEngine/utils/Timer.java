package org.dopelegend.multiItemDisplayEngine.utils;


import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;

public class Timer {
    private static long startTime = 0;

    public static void initialize(){
        startTime = System.currentTimeMillis();
    }
    /**
     *
     * Prints the current time since this plugin started in seconds (with 3 decimals) to the console.
     *
     * @param message The message to print alongside the time
     * @param warning Whether this should be printed as a warning.
     */
    public static void printCurrentTime(String message, boolean warning) {
        long currentTime = Math.abs(System.currentTimeMillis()-startTime);
        double currentTimeSeconds = currentTime/1000.0;
        if (warning) {
            MultiItemDisplayEngine.plugin.getLogger().warning(currentTimeSeconds+" s: "+message);

        }
        MultiItemDisplayEngine.plugin.getLogger().info(currentTimeSeconds+" s: "+message);
    }
}
