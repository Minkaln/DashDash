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
    private double userMenuVolume = 1.0;

    // Track the actual music object so we can stop it specifically
    private Music currentMusic;

    public AudioManager() {
        playlist.add("TTEN.wav");
        playlist.add("LELN.wav");
        playlist.add("JANA.wav");
    }

    public void startPlaylist() {
        // SAFETY: If a playlist is already running, don't start a duplicate one!
        if (playlistStarted) return;

        playNextSong();
    }

    private void playNextSong() {
        if (playlist.isEmpty()) return;

        // 1. Stop the specific current song if it exists
        if (currentMusic != null) {
            getAudioPlayer().stopMusic(currentMusic);
        }

        // 2. Extra safety: stop anything else in the music channel
        getAudioPlayer().stopAllMusic();

        fadeMultiplier = 0;
        userMenuVolume = getSettings().getGlobalMusicVolume();
        getSettings().setGlobalMusicVolume(0);

        List<String> availableSongs = new ArrayList<>(playlist);
        if (availableSongs.size() > 1) {
            availableSongs.remove(lastPlayedSong);
        }

        Collections.shuffle(availableSongs);
        String nextSong = availableSongs.get(0);
        lastPlayedSong = nextSong;

        try {
            // Store the music object so we have a direct handle on it
            currentMusic = getAssetLoader().loadMusic(nextSong);
            getAudioPlayer().playMusic(currentMusic);
            System.out.println("Now Playing: " + nextSong);
        } catch (Exception e) {
            System.out.println("Playlist Error: " + nextSong);
        }

        musicTimer = 0;
        playlistStarted = true;

        currentSongDuration = switch (nextSong) {
            case "TTEN.wav" -> 95.0;
            case "LELN.wav" -> 80.0;
            case "JANA.wav" -> 93.0;
            default -> 100.0;
        };
    }

    public void onUpdate(double tpf) {
        if (!playlistStarted) return;

        musicTimer += tpf;

        // Fading Logic
        if (musicTimer >= (currentSongDuration - 3.0)) {
            fadeMultiplier -= tpf * 0.35;
        } else if (musicTimer <= 3.0) {
            fadeMultiplier += tpf * 0.35;
        } else {
            fadeMultiplier = 1.0;
        }

        fadeMultiplier = Math.max(0, Math.min(1, fadeMultiplier));

        if (fadeMultiplier < 1.0) {
            getSettings().setGlobalMusicVolume(userMenuVolume * fadeMultiplier);
        } else {
            userMenuVolume = getSettings().getGlobalMusicVolume();
        }

        if (musicTimer >= currentSongDuration) {
            playlistStarted = false; // Reset to allow next song to trigger
            playNextSong();
        }
    }

    // --- STATIC SOUND EFFECTS ---
    public static void playHoverSound() {
        play("hover.wav");
    }

    public static void playJumpSound() {
        play("jump_sfx.wav");
    }

    public static void playCrashSound() {
        play("crash.wav");
    }
}