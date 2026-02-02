package com.group.game.dashdash;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import static com.group.game.dashdash.EntityType.PLAYER;

public class GGApplication extends GameApplication {

    private PlayerComponent playerComponent;
    private AudioManager audioManager;

    private SaveData saveData;
    private static final String SAVE_FILE = "save_data.dat";

    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("DashDash");
        settings.setVersion("0.1.1");
        settings.setTicksPerSecond(60);
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new MenuFactory());
    }

    @Override
    protected void onPreInit() {
        double savedVolume = getSettings().getGlobalMusicVolume();
        UserPrefs.setMasterVolume(savedVolume);

        audioManager = new AudioManager();
        audioManager.startPlaylist();

        if (getFileSystemService().exists(SAVE_FILE)) {
            saveData = (SaveData) getFileSystemService().readDataTask(SAVE_FILE).run();
        } else {
            saveData = new SaveData();
            saveGame();
        }
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                if (playerComponent != null) {
                    playerComponent.flipGravity();
                    AudioManager.playJumpSound();
                }
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("mode", GameMode.Endless);
        vars.put("stageColor", Color.BLACK);
        vars.put("score", 0.0); // Changed to 0.0 because TPF results in doubles
        vars.put("highscore", (double) saveData.highscore);
    }

    @Override
    protected void initGame() {
        initBackground();
        entityBuilder().with(new Floor()).buildAndAttach();
        initPlayer();
    }

    private void initBackground() {
        var url = getClass().getResource("/assets/textures/background.png");
        if (url == null) return;
        Image bgImage = new Image(url.toExternalForm());
        ImageView backgroundView = new ImageView(bgImage);
        backgroundView.setFitWidth(getAppWidth());
        backgroundView.setFitHeight(getAppHeight());
        Entity bg = entityBuilder().view(backgroundView).zIndex(-100).buildAndAttach();
        bg.xProperty().bind(getGameScene().getViewport().xProperty());
        bg.yProperty().bind(getGameScene().getViewport().yProperty());
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EntityType.FLOOR) {
            @Override
            protected void onCollision(Entity player, Entity floor) {
                // Snap logic
                if (player.getY() > getAppHeight() / 2.0) {
                    player.setY(floor.getY() - player.getHeight());
                } else {
                    player.setY(floor.getBottomY());
                }
                if (playerComponent != null) playerComponent.setOnSurface(true);
            }
        });

        // Deadly walls
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity player, Entity wall) {
                onPlayerDied();
            }
        });
    }

    @Override
    protected void initUI() {
        Text uiScore = new Text("");
        uiScore.setFont(Font.font(72));
        uiScore.setTranslateX(getAppWidth() - 200);
        uiScore.setTranslateY(100); // Shifted up slightly
        uiScore.setFill(Color.WHITE);
        uiScore.textProperty().bind(getip("score").asString());

        // Use asString("%.0f") to keep the UI showing whole numbers
        uiScore.textProperty().bind(getdp("score").asString("%.0f"));

        Text uiHighscore = new Text("");
        uiHighscore.setFont(Font.font(24));
        uiHighscore.setTranslateX(getAppWidth() - 200);
        uiHighscore.setTranslateY(140);
        uiHighscore.setFill(Color.WHITE);
        uiHighscore.textProperty().bind(getip("highscore").asString().concat(" (Best)"));

        addUINode(uiScore);
        addUINode(uiHighscore);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!getWorldProperties().exists("score")) return;

        // FIXED: Smoother score calculation using Delta Time (tpf)
        double pointsPerSecond = 60;
        inc("score", pointsPerSecond * tpf);

        if (audioManager != null) {
            audioManager.onUpdate(tpf);
        }
    }

    private void saveGame() {
        getFileSystemService().writeDataTask(saveData, SAVE_FILE);
    }

    public void onPlayerDied() {
        AudioManager.playCrashSound();

        // --- FIX: Use getd() instead of geti() ---
        int finalScore = (int) getd("score");

        if (finalScore > saveData.highscore) {
            saveData.highscore = finalScore;
            // Highscore is stored as double in initGameVars for UI binding
            set("highscore", (double) finalScore);
            saveGame();
        }

        getGameController().pauseEngine();

        getDialogService().showConfirmationBox("Game Over! Score: " + finalScore + "\nTry Again?", answer -> {
            getGameController().resumeEngine();

            if (audioManager != null) {
                audioManager.forceNextSong();
            }

            if (answer) {
                runOnce(() -> {
                    getGameController().startNewGame();
                    return null;
                }, Duration.seconds(0.1));
            } else {
                getGameController().gotoMainMenu();
            }
        });
    }

    private void initPlayer() {
        playerComponent = new PlayerComponent();
        Rectangle cube = new Rectangle(70, 60, Color.DODGERBLUE);
        cube.setArcWidth(6);
        cube.setArcHeight(6);
        Rectangle leftEye = new Rectangle(8, 8, Color.BLACK);
        Rectangle rightEye = new Rectangle(8, 8, Color.BLACK);
        leftEye.setTranslateX(18); leftEye.setTranslateY(18);
        rightEye.setTranslateX(44); rightEye.setTranslateY(18);
        Text mouth = new Text("O");
        mouth.setFill(Color.BLACK);
        mouth.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        mouth.setTranslateX(26); mouth.setTranslateY(42);
        Group playerView = new Group(cube, leftEye, rightEye, mouth);

        Entity player = entityBuilder()
                .at(0, 0)
                .type(PLAYER)
                .bbox(new HitBox(BoundingShape.box(70, 60)))
                .view(playerView)
                .collidable()
                .with(playerComponent, new WallBuildingComponent(), new Floor())
                .buildAndAttach();

        getGameScene().getViewport().setBounds(0, 0, Integer.MAX_VALUE, getAppHeight());
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 3.0, getAppHeight() / 2.0);

        animationBuilder()
                .duration(Duration.seconds(0.86))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .scale(player)
                .from(new Point2D(0, 0))
                .to(new Point2D(1, 1))
                .buildAndPlay();
    }

    public static void main(String[] args) {
        System.setProperty("prism.allowhidpi", "false");
        System.setProperty("javafx.animation.pulse", "10");
        launch(args);
    }
}