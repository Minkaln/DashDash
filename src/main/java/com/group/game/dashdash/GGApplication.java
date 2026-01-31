package com.group.game.dashdash;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
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

    // ✅ KEEP THIS AS A FIELD (IMPORTANT)
    private MediaPlayer bgmPlayer;

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

    // 🎵 FIXED BGM METHOD
    private void playBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }

        var url = getClass().getResource("/assets/music/bgm.mp3");

        if (url == null) {
            System.out.println("❌ BGM not found!");
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
        initBackground();

        entityBuilder()
                .with(new Floor())
                .buildAndAttach();

        playBGM(); // 🎵 now works correctly

        initPlayer();
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

        GameMode mode = geto("mode");
        int level = geti("level");

        if (mode == GameMode.Classic) {
            int winCondition = level * 2000;
            if (geti("score") >= winCondition) {
                showWinMessage();
            }
        }
    }

    private void initBackground() {
        Rectangle rect = new Rectangle(getAppWidth(), getAppHeight(), Color.WHITE);

        Entity bg = entityBuilder()
                .view(rect)
                .with("rect", rect)
                .with(new ColorChangingComponent())
                .buildAndAttach();

        bg.xProperty().bind(getGameScene().getViewport().xProperty());
        bg.yProperty().bind(getGameScene().getViewport().yProperty());
    }

    private void initPlayer() {
        playerComponent = new PlayerComponent();

        Rectangle cube = new Rectangle(70, 60);
        cube.setFill(Color.DODGERBLUE);
        cube.setArcWidth(6);
        cube.setArcHeight(6);

        Entity player = entityBuilder()
                .at(0, 0)
                .type(PLAYER)
                .bbox(new HitBox(BoundingShape.box(70, 60)))
                .view(cube)
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

    private void showWinMessage() {
        showMessage("Level " + geti("level") + " Complete!", () -> {
            getGameController().gotoMainMenu();
            return null;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
