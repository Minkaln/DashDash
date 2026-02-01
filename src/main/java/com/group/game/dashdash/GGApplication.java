package com.group.game.dashdash;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
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
    private boolean requestNewGame = false;
    private AudioManager audioManager;

    // --- SAVE SYSTEM VARIABLES ---
    private SaveData saveData;
    private static final String SAVE_FILE = "save_data.dat";

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("DashDash");
        settings.setVersion("0.0.10");
        settings.setTicksPerSecond(60);
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new MenuFactory());
    }
    @Override
    protected void onPreInit() {
        audioManager = new AudioManager();
        audioManager.startPlaylist();

        if (getFileSystemService().exists(SAVE_FILE)) {
            // Load existing progress
            saveData = (SaveData) getFileSystemService().readDataTask(SAVE_FILE).run();
            System.out.println("Save data loaded successfully.");
        } else {
            // Create new progress for a new player
            saveData = new SaveData();
            saveGame(); // Create the physical file immediately
            System.out.println("No save file found. Created a new one.");
        }
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                if (playerComponent != null) {
                    playerComponent.flipGravity();
                    audioManager.playJumpSound();
                }
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("mode", GameMode.Endless);
        vars.put("level", 1);
        vars.put("stageColor", Color.BLACK);
        vars.put("score", 0);
        // 2. Add highscore to game variables so UI can bind to it
        vars.put("highscore", saveData.highscore);
    }

    @Override
    protected void initGame() {
        initBackground();
        entityBuilder().with(new Floor()).buildAndAttach();
        initPlayer();
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
                playerComponent.setOnSurface(true);
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity player, Entity wall) {
                requestNewGame();
            }
        });
    }

    @Override
    protected void initUI() {
        // Current Score
        Text uiScore = new Text("");
        uiScore.setFont(Font.font(72));
        uiScore.setTranslateX(getAppWidth() - 200);
        uiScore.setTranslateY(100); // Shifted up slightly
        uiScore.fillProperty().bind(getop("stageColor"));
        uiScore.textProperty().bind(getip("score").asString());

        // 3. High Score Display
        Text uiHighscore = new Text("");
        uiHighscore.setFont(Font.font(24));
        uiHighscore.setTranslateX(getAppWidth() - 200);
        uiHighscore.setTranslateY(140);
        uiHighscore.setFill(Color.GRAY);
        uiHighscore.textProperty().bind(getip("highscore").asString().concat(" (Best)"));

        addUINode(uiScore);
        addUINode(uiHighscore);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (requestNewGame) {
            requestNewGame = false;
            getGameController().startNewGame();
            return;
        }

        inc("score", +1);
        audioManager.onUpdate(tpf);

        GameMode mode = geto("mode");
        int level = geti("level");
        if (mode == GameMode.Classic) {
            int winCondition = level * 2000;
            if (geti("score") >= winCondition) {
                showWinMessage();
            }
        }
    }

    private void showWinMessage() {
        // 4. Update unlocked levels progress
        int currentLevel = geti("level");
        if (currentLevel >= saveData.unlockedLevel) {
            saveData.unlockedLevel = currentLevel + 1;
            saveGame();
        }

        showMessage("Level " + geti("level") + " Complete!", () -> {
            getGameController().gotoMainMenu();
            return null;
        });
    }

    private void saveGame() {
        getFileSystemService().writeDataTask(saveData, SAVE_FILE);
    }

    public void requestNewGame() {
        audioManager.playCrashSound();

        // 5. Check and save highscore before restarting
        int finalScore = geti("score");
        if (finalScore > saveData.highscore) {
            saveData.highscore = finalScore;
            set("highscore", finalScore); // Update visible UI
            saveGame();
        }

        requestNewGame = true;
    }

    // ... initBackground, initPlayer, and main remain the same ...

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
                .with(playerComponent, new WallBuildingComponent(), new Floor())
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

    public static void main(String[] args) {
        System.setProperty("prism.allowhidpi", "false");
        launch(args);
    }
}