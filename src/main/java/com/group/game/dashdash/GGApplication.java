package com.group.game.dashdash;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
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
import java.io.File;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import static com.group.game.dashdash.EntityType.PLAYER;

public class GGApplication extends GameApplication {

    private Entity bg1;
    private Entity bg2;
    private Entity bg3;
    private double bgWidth;
    private PlayerComponent playerComponent;
    private AudioManager audioManager;

    private SaveData saveData = new SaveData();
    private static final String SAVE_FILE = System.getProperty("user.home") + File.separator + "dashdash_save.properties";
    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("DashDash");
        settings.setVersion("1.0.0");
        settings.setTicksPerSecond(60);
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new MenuFactory());
    }

    @Override
    protected void onPreInit() {
        java.io.File file = new java.io.File(SAVE_FILE);

        if (file.exists()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                java.util.Properties props = new java.util.Properties();
                props.load(in);

                // Restore Highscore
                saveData.highscore = Integer.parseInt(props.getProperty("highscore", "0"));

                // Restore Volume to your UserPrefs class
                double savedVol = Double.parseDouble(props.getProperty("volume", "0.5"));
                UserPrefs.setMasterVolume(savedVol);

                // Sync the game engine to this volume
                getSettings().setGlobalMusicVolume(savedVol);
                getSettings().setGlobalSoundVolume(savedVol);

            } catch (Exception e) {
                System.err.println("Could not load save: " + e.getMessage());
            }
        }

        // Now start the music using the restored volume
        audioManager = new AudioManager();
        audioManager.startPlaylist();
    }
    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                if (playerComponent != null && playerComponent.isOnSurface()) {
                    playerComponent.flipGravity();
                    AudioManager.playJumpSound();
                }
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        // This grabs the value that was just loaded in onPreInit
        vars.put("highscore", (double) saveData.highscore);

        vars.put("score", 0.0);
        vars.put("mode", GameMode.Endless);
        vars.put("stageColor", Color.BLACK);
        vars.put("currentBgName", "gd_bg3.jpg");
    }

    @Override
    protected void initGame() {
        initBackground();
        entityBuilder().with(new Floor()).buildAndAttach();
        initPlayer();
    }

    private void initBackground() {
        // Pool of available backgrounds
        String[] bgPool = {"gd_bg1.jpg", "gd_bg2.jpg", "gd_bg3.jpg"};

        // 25% chance to switch to a new random background from the pool
        if (FXGLMath.randomBoolean(0.25)) {
            String randomBg = bgPool[FXGLMath.random(0, bgPool.length - 1)];
            set("currentBgName", randomBg);
        }

        String selectedFile = ("currentBgName");
        var url = getClass().getResource("/assets/textures/" + selectedFile);

        // Safety fallback if file is missing
        if (url == null) {
            url = getClass().getResource("/assets/textures/gd_bg3.jpg");
            if (url == null) return;
        }

        Image bgImage = new Image(url.toExternalForm());
        bgWidth = bgImage.getWidth();

        // Piece 1
        bg1 = entityBuilder()
                .view(new ImageView(bgImage))
                .zIndex(-100)
                .buildAndAttach();

        // Piece 2
        bg2 = entityBuilder()
                .at(bgWidth, 0)
                .view(new ImageView(bgImage))
                .zIndex(-100)
                .buildAndAttach();

        // Piece 3
        bg3 = entityBuilder()
                .at(bgWidth * 2, 0)
                .view(new ImageView(bgImage))
                .zIndex(-100)
                .buildAndAttach();
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EntityType.FLOOR) {
            @Override
            protected void onCollision(Entity player, Entity floor) {
                if (player.getY() > getAppHeight() / 2.0) {
                    player.setY(floor.getY() - player.getHeight());
                } else {
                    player.setY(floor.getBottomY());
                }

                if (playerComponent != null) {
                    playerComponent.setTouchingSurface(true);
                }
            }
        });

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
        uiScore.setTranslateY(100);
        uiScore.setFill(Color.WHITE);

        uiScore.textProperty().bind(getdp("score").asString("%.0f"));

        Text uiHighscore = new Text("");
        uiHighscore.setFont(Font.font(24));
        uiHighscore.setTranslateX(getAppWidth() - 200);
        uiHighscore.setTranslateY(140);
        uiHighscore.setFill(Color.WHITE);

        uiHighscore.textProperty().bind(getdp("highscore").asString("%.0f").concat(" (Best)"));

        addUINode(uiScore);
        addUINode(uiHighscore);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!getWorldProperties().exists("score")) return;

        double pointsPerSecond = 60;
        inc("score", pointsPerSecond * tpf);


        if (bg1 != null && bg2 != null && bg3 != null) {
            double viewX = getGameScene().getViewport().getX();
            double viewY = getGameScene().getViewport().getY();

            double parallaxX = viewX * 0.8;
            double xOffset = parallaxX % bgWidth;

            bg1.setX(viewX - xOffset);
            bg2.setX(viewX - xOffset + bgWidth);
            bg3.setX(viewX - xOffset + (bgWidth * 2));

            bg1.setY(viewY);
            bg2.setY(viewY);
            bg3.setY(viewY);
        }

        if (audioManager != null) {
            audioManager.onUpdate(tpf);

       }
    }
    public void saveGame() {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File file = new java.io.File(SAVE_FILE);

            // 1. Pack the data
            props.setProperty("highscore", String.valueOf(saveData.highscore));
            props.setProperty("volume", String.valueOf(UserPrefs.getMasterVolume()));
            props.setProperty("unlockedLevel", String.valueOf(saveData.unlockedLevel));

            // 2. Write to the file
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DashDash Save Data");
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public void onPlayerDied() {
        AudioManager.playCrashSound();

        int finalScore = (int) getd("score");

        // Check if score is actually higher
        if (finalScore > saveData.highscore) {
            System.out.println("New Highscore! Old: " + saveData.highscore + " New: " + finalScore);
            saveData.highscore = finalScore;
            set("highscore", (double) finalScore);
            saveGame(); // Ensure this is definitely being called
        } else {
            System.out.println("Score too low to save. Score: " + finalScore + " Best: " + saveData.highscore);
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