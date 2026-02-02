package com.group.game.dashdash;

public class UserPrefs {
    private static double masterVolume = 0.5;

    public static void setMasterVolume(double volume) {
        // Clamp to valid range
        masterVolume = Math.max(0, Math.min(1, volume));
    }

    public static double getMasterVolume() {
        return masterVolume;
    }
}