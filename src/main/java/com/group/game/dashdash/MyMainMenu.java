package com.group.game.dashdash;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MyMainMenu extends FXGLMenu {

    public MyMainMenu() {
        super(MenuType.MAIN_MENU);

        // Background
        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        getContentRoot().getChildren().add(bg);

        // Title
        Text title = FXGL.getUIFactoryService().newText("DASH DASH", Color.WHITE, 60);
        title.setTranslateX(FXGL.getAppWidth() / 2.0 - 150);
        title.setTranslateY(150);
        getContentRoot().getChildren().add(title);

        // --- START GAME BUTTON (Formerly Endless Mode) ---
        var btnStart = FXGL.getUIFactoryService().newButton("START GAME");
        btnStart.setTranslateX(FXGL.getAppWidth() / 2.0 - 100);
        btnStart.setTranslateY(350); // Positioned more centrally since levels are gone

        btnStart.setOnMouseEntered(e -> AudioManager.playHoverSound());

        btnStart.setOnAction(e -> {
            FXGL.set("mode", GameMode.Endless);
            fireNewGame();
        });

        getContentRoot().getChildren().add(btnStart);

        // --- DEBUG BUTTON ---
        var btnDebug = FXGL.getUIFactoryService().newButton("DEBUG: PLAY TTEN");
        btnDebug.setTranslateX(20);
        btnDebug.setTranslateY(FXGL.getAppHeight() - 60);

        btnDebug.setOnMouseEntered(e -> AudioManager.playHoverSound());

        btnDebug.setOnAction(e -> {
            try {
                var music = FXGL.getAssetLoader().loadMusic("TTEN.wav");
                FXGL.getAudioPlayer().playMusic(music);
            } catch (Exception err) {
                System.out.println("Debug Playback Failed: " + err.getMessage());
            }
        });
        getContentRoot().getChildren().add(btnDebug);
    }
}