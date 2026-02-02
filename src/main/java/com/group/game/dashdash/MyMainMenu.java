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
import javafx.scene.layout.VBox;
import javafx.scene.control.Slider;
import java.util.Objects;

public class MyMainMenu extends FXGLMenu {

    public MyMainMenu() {
        super(MenuType.MAIN_MENU);

        /* ================= VIDEO BACKGROUND & OVERLAY ================= */
        var url = Objects.requireNonNull(MyMainMenu.class.getResource("/assets/videos/menu_bg.mp4")).toExternalForm();
        var player = new MediaPlayer(new Media(url));
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setMute(true);
        player.setOnReady(player::play);
        var videoView = new MediaView(player);
        videoView.setFitWidth(FXGL.getAppWidth());
        videoView.setFitHeight(FXGL.getAppHeight());
        videoView.setPreserveRatio(false);

        var overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.rgb(0, 0, 0, 0.35));
        overlay.setMouseTransparent(true);

        getContentRoot().getChildren().addAll(videoView, overlay);

        /* ================= TITLE ================= */
        Text title = FXGL.getUIFactoryService().newText("DASH DASH", Color.WHITE, 60);
        title.setTranslateX(FXGL.getAppWidth() / 2.1 - 150);
        title.setTranslateY(150);
        title.setStroke(Color.BLACK);
        title.setStrokeWidth(3);
        title.setEffect(new DropShadow(15, Color.BLACK));
        getContentRoot().getChildren().add(title);

        /* ================= START BUTTON ================= */
        var btnEndless = FXGL.getUIFactoryService().newButton("START");
        btnEndless.setTranslateX(FXGL.getAppWidth() / 1.63 - 250);
        btnEndless.setTranslateY(280);
        btnEndless.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold");
        btnEndless.setOnAction(e -> {
            FXGL.set("mode", GameMode.Endless);
            fireNewGame();
        });

        /* ================= SETTINGS BUTTON ================= */
        var btnSettings = FXGL.getUIFactoryService().newButton("SETTINGS");
        btnSettings.setTranslateX(FXGL.getAppWidth() / 1.63 - 250);
        btnSettings.setTranslateY(350);
        btnSettings.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold");

        /* ================= EXIT BUTTON ================= */
        var btnExit = FXGL.getUIFactoryService().newButton("EXIT");
        btnExit.setTranslateX(FXGL.getAppWidth() / 1.63 - 250);
        btnExit.setTranslateY(420); // Moved down to avoid overlap
        btnExit.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold");
        btnExit.setOnAction(e -> FXGL.getGameController().exit());

        /* ================= SETTINGS PANEL (Hidden by default) ================= */
        var settingsBox = new VBox(15);
        settingsBox.setTranslateX(FXGL.getAppWidth() / 1.63 - 250);
        settingsBox.setTranslateY(300);
        settingsBox.setVisible(false);

        Text volLabel = FXGL.getUIFactoryService().newText("VOLUME", Color.WHITE, 20);
        Slider slider = new Slider(0, 1, UserPrefs.getMasterVolume());
        slider.setPrefWidth(200);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            UserPrefs.setMasterVolume(vol);
            FXGL.getSettings().setGlobalMusicVolume(vol);
            FXGL.getSettings().setGlobalSoundVolume(vol);
            ((GGApplication) FXGL.getApp()).saveGame();
        });

        var btnBack = FXGL.getUIFactoryService().newButton("BACK");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold");

        settingsBox.getChildren().addAll(volLabel, slider, btnBack);

        /* ================= LOGIC TO SWITCH VIEWS ================= */
        btnSettings.setOnAction(e -> {
            btnEndless.setVisible(false);
            btnSettings.setVisible(false);
            btnExit.setVisible(false);
            settingsBox.setVisible(true);
        });

        btnBack.setOnAction(e -> {
            settingsBox.setVisible(false);
            btnEndless.setVisible(true);
            btnSettings.setVisible(true);
            btnExit.setVisible(true);
        });

        // Add everything to the screen
        getContentRoot().getChildren().addAll(btnEndless, btnSettings, btnExit, settingsBox);
    }
}