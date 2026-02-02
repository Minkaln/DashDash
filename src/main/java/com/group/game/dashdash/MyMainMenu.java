package com.group.game.dashdash;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.effect.DropShadow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Objects;

public class MyMainMenu extends FXGLMenu {

    public MyMainMenu() {
        super(MenuType.MAIN_MENU);

        /* ================= VIDEO BACKGROUND ================= */

        var url = Objects.requireNonNull(
                MyMainMenu.class.getResource("/assets/videos/menu_bg.mp4")
        ).toExternalForm();

        var media = new Media(url);
        var player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setMute(true);
        player.setOnReady(player::play);

        var videoView = new MediaView(player);
        videoView.setFitWidth(FXGL.getAppWidth());
        videoView.setFitHeight(FXGL.getAppHeight());
        videoView.setPreserveRatio(false);

        getContentRoot().getChildren().add(videoView);

        /* ================= DARK OVERLAY (for contrast) ================= */

        var overlay = new Rectangle(
                FXGL.getAppWidth(),
                FXGL.getAppHeight(),
                Color.rgb(0, 0, 0, 0.35)
        );
        overlay.setMouseTransparent(true);
        getContentRoot().getChildren().add(overlay);

        /* ================= TITLE ================= */

        Text title = FXGL.getUIFactoryService()
                .newText("DASH DASH", Color.WHITE, 60);

        title.setTranslateX(FXGL.getAppWidth() / 2.1 - 150);
        title.setTranslateY(150);

        title.setStroke(Color.BLACK);
        title.setStrokeWidth(3);
        title.setEffect(new DropShadow(15, Color.BLACK));

        getContentRoot().getChildren().add(title);

        /* ================= START TEXT BUTTON ================= */
        var btnEndless = FXGL.getUIFactoryService().newButton("START");
        btnEndless.setTranslateX(FXGL.getAppWidth() / 1.63 - 250);
        btnEndless.setTranslateY(300);
        btnEndless.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold"
        );
        btnEndless.setEffect(new DropShadow(5, Color.BLACK));
        btnEndless.setOnMouseEntered(_ -> AudioManager.playHoverSound());
        btnEndless.setOnAction(_ -> {
            FXGL.set("mode", GameMode.Endless);
            fireNewGame();
        });


        /* ================= EXIT TEXT BUTTON ================= */
        var btnExit = FXGL.getUIFactoryService().newButton("EXIT");
        btnExit.setTranslateX(FXGL.getAppWidth() / 1.63 - 250);
        btnExit.setTranslateY(380); // below SETTINGS
        btnExit.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold"
        );
        btnExit.setEffect(new DropShadow(5, Color.BLACK));
        btnExit.setOnMouseEntered(_ -> AudioManager.playHoverSound());
        btnExit.setOnAction(_ -> FXGL.getGameController().exit()); // Proper exit


// Add all buttons to the UI
        getContentRoot().getChildren().addAll(btnEndless, btnExit);

        /* ================= DEBUG BUTTON ================= */

        var btnDebug = FXGL.getUIFactoryService()
                .newButton("DEBUG: PLAY TTEN");

        btnDebug.setTranslateX(20);
        btnDebug.setTranslateY(FXGL.getAppHeight() - 60);

        btnDebug.setStyle(
                "-fx-background-color: rgba(0,0,0,0.4);" +
                        "-fx-text-fill: white;"
        );

        btnDebug.setEffect(new DropShadow(6, Color.BLACK));
        btnDebug.setOnMouseEntered(_ -> AudioManager.playHoverSound());

        btnDebug.setOnAction(_ -> {
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
