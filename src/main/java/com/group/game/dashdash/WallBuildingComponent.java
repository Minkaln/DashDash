package com.group.game.dashdash;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WallBuildingComponent extends Component {

    private double lastWall = 1000;
    private final double FLOOR_THICKNESS = 50;

    @Override
    public void onUpdate(double tpf) {
        GameMode mode = geto("mode");
        int level = geti("level");

        // --- SCORE-BASED STOP CHECK ---
        // If in Classic mode, stop building walls once the score target is reached
        if (mode == GameMode.Classic) {
            int winScore = level * 2000;
            if (geti("score") >= winScore) {
                return; // Stop spawning so the player can sail to victory
            }
        }

        if (lastWall - entity.getX() < getAppWidth()) {
            buildWalls();
        }
    }

    private void buildWalls() {
        GameMode mode = geto("mode");
        int level = geti("level");

        // 1. Calculate current speed based on Mode and Level
        double currentSpeed = (mode == GameMode.Endless) ? 400 : (400 + (level * 50));

        // 2. Calculate gap but cap it at 1500 pixels.
        // This gives them 1.8 seconds of reaction time, but won't let the
        // world feel empty if the speed gets super high.
        double gapBetweenObstacles = Math.min(1500, currentSpeed * 1.8);

        double screenHeight = getAppHeight();
        double wallWidth = 50;
        double playableHeight = screenHeight - (FLOOR_THICKNESS * 2);

        // 3. Keep the vertical passage fair.
        // As the player goes faster, we give them a slightly larger hole to fly through.
        double playerPassage = 320 + (currentSpeed * 0.1);

        for (int i = 1; i <= 5; i++) {
            double spawnX = lastWall + i * gapBetweenObstacles;
            double chance = Math.random();

            if (chance < 0.25) {
                // BOTH WALLS
                double totalWallSpace = playableHeight - playerPassage;
                double topWallHeight = random(50, totalWallSpace - 50);
                double bottomWallHeight = totalWallSpace - topWallHeight;

                spawnWall(spawnX, FLOOR_THICKNESS, wallWidth, topWallHeight);
                spawnWall(spawnX, screenHeight - FLOOR_THICKNESS - bottomWallHeight, wallWidth, bottomWallHeight);

            } else if (chance < 0.60) {
                // FLOOR ONLY
                double wallHeight = random(100, 400);
                spawnWall(spawnX, screenHeight - FLOOR_THICKNESS - wallHeight, wallWidth, wallHeight);

            } else if (chance < 0.95) {
                // CEILING ONLY
                double wallHeight = random(100, 400);
                spawnWall(spawnX, FLOOR_THICKNESS, wallWidth, wallHeight);
            }
            // 5% chance of a pure empty stretch
        }

        lastWall += 5 * gapBetweenObstacles;
    }

    private void spawnWall(double x, double y, double w, double h) {
        entityBuilder()
                .at(x, y)
                .type(EntityType.WALL)
                .viewWithBBox(wallView(w, h))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    private Rectangle wallView(double width, double height) {
        Rectangle wall = new Rectangle(width, height);
        wall.setArcWidth(10);
        wall.setArcHeight(10);
        wall.fillProperty().bind(getWorldProperties().objectProperty("stageColor"));
        return wall;
    }
}