package com.group.game.dashdash;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import static com.group.game.dashdash.EntityType.*;

public class GGApplication extends GameApplication {

    private PlayerComponent playerComponent;
    private boolean requestNewGame = false;

    // 🎵 Keep BGM alive
    private MediaPlayer bgmPlayer;

    // 🖼 Keep background alive
    private ImageView backgroundView;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("DashDash");
        settings.setVersion("0.0.5");
        settings.setTicksPerSecond(60);
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new MenuFactory());
    }

    @Override
    protected void initInput() {
        getInput().addAction(new com.almasb.fxgl.input.UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                if (playerComponent != null) {
                    playerComponent.flipGravity();
                }
            }
        }, javafx.scene.input.KeyCode.SPACE);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("mode", GameMode.Endless);
        vars.put("level", 1);
        vars.put("stageColor", Color.BLACK);
        vars.put("score", 0);
    }

    // 🎵 BGM
    private void playBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }

        var url = getClass().getResource("/assets/music/bgm.mp3");

        if (url == null) {
            System.out.println("❌ BGM not found");
            return;
        }

        Media media = new Media(url.toExternalForm());
        bgmPlayer = new MediaPlayer(media);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgmPlayer.setVolume(0.5);
        bgmPlayer.play();
    }

    @Override
    protected void initGame() {
        initBackground();   // 🖼 PNG background
        playBGM();          // 🎵 music

        entityBuilder()
                .with(new Floor())
                .buildAndAttach();

        initPlayer();
    }

    // 🖼 PNG BACKGROUND (FIXED)
    private void initBackground() {
        var url = getClass().getResource("/assets/textures/background.png");

        if (url == null) {
            System.out.println("❌ Background image not found");
            return;
        }

        Image bgImage = new Image(url.toExternalForm());
        backgroundView = new ImageView(bgImage);

        backgroundView.setFitWidth(getAppWidth());
        backgroundView.setFitHeight(getAppHeight());
        backgroundView.setPreserveRatio(false);

        Entity bg = entityBuilder()
                .view(backgroundView)
                .zIndex(-100) // stay behind everything
                .buildAndAttach();

        // Follow camera
        bg.xProperty().bind(getGameScene().getViewport().xProperty());
        bg.yProperty().bind(getGameScene().getViewport().yProperty());
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, FLOOR) {
            @Override
            protected void onCollision(Entity player, Entity floor) {
                if (player.getY() > getAppHeight() / 2.0) {
                    player.setY(floor.getY() - player.getHeight());
                } else {
                    player.setY(floor.getBottomY());
                }
                playerComponent.setOnSurface(true);
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, WALL) {
            @Override
            protected void onCollisionBegin(Entity player, Entity wall) {
                requestNewGame();
            }
        });
    }

    @Override
    protected void initUI() {
        Text uiScore = new Text();
        uiScore.setFont(Font.font(72));
        uiScore.setTranslateX(getAppWidth() - 200);
        uiScore.setTranslateY(160);

        uiScore.fillProperty().bind(getop("stageColor"));
        uiScore.textProperty().bind(getip("score").asString());

        addUINode(uiScore);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (requestNewGame) {
            requestNewGame = false;
            getGameController().startNewGame();
            return;
        }

        inc("score", 1);
    }

    private void initPlayer() {
        playerComponent = new PlayerComponent();

        // 🟦 Player body
        Rectangle cube = new Rectangle(70, 60);
        cube.setFill(Color.DODGERBLUE);
        cube.setArcWidth(6);
        cube.setArcHeight(6);

        // 👀 Eyes
        Rectangle leftEye = new Rectangle(8, 8, Color.BLACK);
        Rectangle rightEye = new Rectangle(8, 8, Color.BLACK);

        leftEye.setTranslateX(18);
        leftEye.setTranslateY(18);

        rightEye.setTranslateX(44);
        rightEye.setTranslateY(18);

        // 👄 Mouth (._.)
        Text mouth = new Text("O");
        mouth.setFill(Color.BLACK);
        mouth.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        mouth.setTranslateX(26);
        mouth.setTranslateY(42);

        // 🧩 Combine face + body
        Group playerView = new Group(
                cube,
                leftEye,
                rightEye,
                mouth
        );

        Entity player = entityBuilder()
                .at(0, 0)
                .type(PLAYER)
                .bbox(new HitBox(BoundingShape.box(70, 60)))
                .view(playerView)
                .collidable()
                .with(playerComponent, new WallBuildingComponent())
                .buildAndAttach();

        getGameScene().getViewport().setBounds(0, 0, Integer.MAX_VALUE, getAppHeight());
        getGameScene().getViewport().bindToEntity(
                player,
                getAppWidth() / 3.0,
                getAppHeight() / 2.0
        );

        animationBuilder()
                .duration(Duration.seconds(0.86))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .scale(player)
                .from(new Point2D(0, 0))
                .to(new Point2D(1, 1))
                .buildAndPlay();
    }


    public void requestNewGame() {
        requestNewGame = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
