package com.group.game.dashdash;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import static com.group.game.dashdash.EntityType.PLAYER;
import static com.group.game.dashdash.EntityType.WALL;


public class GGApplication extends GameApplication {

    private PlayerComponent playerComponent;
    private boolean requestNewGame = false;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("DashDash");
        settings.setVersion("0.0.5");
        settings.setTicksPerSecond(60); //framerate important :D
        settings.setMainMenuEnabled(true); // Optional: keeps it simple for testing
        settings.setSceneFactory(new MenuFactory());
    }
    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                if (playerComponent != null) {
                    playerComponent.flipGravity();
                }
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        // These MUST be here or the components will crash!
        vars.put("mode", GameMode.Endless); // Default starting mode
        vars.put("level", 1);               // Default starting level

        vars.put("stageColor", Color.BLACK);
        vars.put("score", 0);
    }

    @Override
    protected void onPreInit() {
        // Ensure assets/music/bgm.mp3 exists
        loopBGM("bgm.mp3");
    }

    @Override
    protected void initGame() {
        initBackground();
        entityBuilder()
                .with(new Floor())
                .buildAndAttach();
        initPlayer();
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

                playerComponent.setOnSurface(true);
            }
        });

        // Deadly walls
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity player, Entity wall) {
                requestNewGame();
            }
        });
    }
    @Override
    protected void initUI() {
        Text uiScore = new Text("");
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

        inc("score", +1);

        // Get the current mode and level
        GameMode mode = geto("mode");
        int level = geti("level");

        if (mode == GameMode.Classic) {
            // Example: Level 1 = 2000 score, Level 2 = 4000 score, etc.
            int winCondition = level * 2000;

            if (geti("score") >= winCondition) {
                showWinMessage();
            }
        }
        // In Endless mode, the score just keeps going forever!
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
        Entity player = entityBuilder()
                .at(0, 0)
                .type(PLAYER)
                .bbox(new HitBox(BoundingShape.box(50, 60)))
                .view(texture("bird.png").toAnimatedTexture(2, Duration.seconds(0.5)).loop())
                .collidable()
                .with(playerComponent, new WallBuildingComponent(), new Floor())
                .buildAndAttach();

        getGameScene().getViewport().setBounds(0, 0, Integer.MAX_VALUE, getAppHeight());
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 3.0, getAppHeight() / 2.0);

        // Standard FXGL way to animate an entity spawn in Java
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
        // Stop the game and show a victory message
        showMessage("Level " + geti("level") + " Complete!", () -> {
            getGameController().gotoMainMenu();
            return null;
        });
    }
    static void main(String[] args) {
        launch(args);
    }
}