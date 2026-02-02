package com.group.game.dashdash;

import com.almasb.fxgl.audio.Music;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class AudioManager {

    private List<String> playlist = new ArrayList<>();
    private String lastPlayedSong = "";
    private double musicTimer = 0;
    private double currentSongDuration = 0;
    private boolean playlistStarted = false;

    private double fadeMultiplier = 0;
    private Music currentMusic;

    // --- NEW: Track if we have captured the user's initial volume ---
    private boolean isInitialized = false;

    // --- NEW: Track the volume we set in the previous frame ---
    private double lastKnownSetting = -1;

    public AudioManager() {
        playlist.add("TTEN.wav");
        playlist.add("LELN.wav");
        playlist.add("JANA.wav");
    }

    public void startPlaylist() {
        if (playlistStarted) return;
        playlistStarted = true;
        playNextSong();
    }

    public void forceNextSong() {
        playNextSong();
    }

    private void playNextSong() {
        if (playlist.isEmpty()) return;

        if (currentMusic != null) {
            getAudioPlayer().stopMusic(currentMusic);
        }
        getAudioPlayer().stopAllMusic();

        List<String> availableSongs = new ArrayList<>(playlist);
        if (availableSongs.size() > 1 && !lastPlayedSong.isEmpty()) {
            availableSongs.remove(lastPlayedSong);
        }
        Collections.shuffle(availableSongs);
        String nextSong = availableSongs.get(0);
        lastPlayedSong = nextSong;

        try {
            currentMusic = getAssetLoader().loadMusic(nextSong);
            getAudioPlayer().playMusic(currentMusic);
        } catch (Exception e) {
            System.err.println("Could not load music: " + nextSong);
        }

        musicTimer = 0;
        fadeMultiplier = 0;
        lastKnownSetting = -1; // Reset tracking for the new song

        currentSongDuration = switch (nextSong) {
            case "TTEN.wav" -> 95.0;
            case "LELN.wav" -> 80.0;
            case "JANA.wav" -> 93.0;
            default -> 100.0;
        };
    }

    public void onUpdate(double tpf) {
        if (!playlistStarted || currentMusic == null) return;

        // --- THE CRITICAL "LOCK" ---
        // If we haven't initialized yet, we try to grab the system volume.
        // We DON'T set the volume yet, so we don't overwrite the user's preference.
        if (!isInitialized) {
            double initialVol = getSettings().getGlobalMusicVolume();
            UserPrefs.setMasterVolume(initialVol);
            isInitialized = true;
            return; // Skip this frame to let the value settle
        }

        musicTimer += tpf;

        double currentEngineVolume = getSettings().getGlobalMusicVolume();

        // Only detect slider movement if we aren't at the very start of the fade
        if (lastKnownSetting != -1 && Math.abs(currentEngineVolume - lastKnownSetting) > 0.001) {
            // Calculate what the "Master" should be based on the current faded position
            double newMaster = currentEngineVolume / Math.max(0.01, fadeMultiplier);
            UserPrefs.setMasterVolume(newMaster);
        }

        // 1. Calculate Fade Multiplier
        if (musicTimer <= 3.0) {
            fadeMultiplier = musicTimer / 3.0;
        } else if (musicTimer >= (currentSongDuration - 3.0)) {
            fadeMultiplier = (currentSongDuration - musicTimer) / 3.0;
        } else {
            fadeMultiplier = 1.0;
            // Middle of song: Keep UserPrefs perfectly in sync with slider
            UserPrefs.setMasterVolume(currentEngineVolume);
        }

        fadeMultiplier = Math.max(0, Math.min(1, fadeMultiplier));

        // 2. APPLY VOLUME
        // We multiply the static UserPrefs by the current fade progress.
        double targetVolume = UserPrefs.getMasterVolume() * fadeMultiplier;

        // Save this value so we can detect manual changes in the next frame
        lastKnownSetting = targetVolume;

        getSettings().setGlobalMusicVolume(targetVolume);

        if (musicTimer >= currentSongDuration) {
            playNextSong();
        }
    }

    public static void playHoverSound() { play("hover.wav"); }
    public static void playJumpSound() { play("jump_sfx.wav"); }
    public static void playCrashSound() { play("crash.wav"); }
}