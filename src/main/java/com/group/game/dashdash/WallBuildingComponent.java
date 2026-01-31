package com.group.game.dashdash;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WallBuildingComponent extends Component {

    private double lastWall = 1000;
    private final double FLOOR_THICKNESS = 50;

    @Override
    public void onUpdate(double tpf) {
        GameMode mode = geto("mode");
        int level = geti("level");

        if (mode == GameMode.Classic) {
            int winScore = level * 2000;
            if (geti("score") >= winScore) {
                return;
            }
        }

        if (lastWall - entity.getX() < getAppWidth()) {
            buildWalls();
        }
    }

    private Polygon wallView(double width, double height) {
        // Pointing UP
        Polygon wall = new Polygon(
                0.0, height,
                width / 2.0, 0.0,
                width, height
        );
        wall.fillProperty().bind(getWorldProperties().objectProperty("stageColor"));
        return wall;
    }

    private Polygon spikeViewDown(double width, double height) {
        // Pointing DOWN
        Polygon spike = new Polygon(
                0.0, 0.0,
                width, 0.0,
                width / 2.0, height
        );
        spike.fillProperty().bind(getWorldProperties().objectProperty("stageColor"));
        return spike;
    }

    private void buildWalls() {
        Entity player = getGameWorld().getSingleton(EntityType.PLAYER);
        PlayerComponent pc = player.getComponent(PlayerComponent.class);
        double currentSpeed = pc.getVelocityX();

        // 1. Spacing between spike sets
        double gapBetweenObstacles = currentSpeed * 1.5;

        // 2. WIDER HOLE: Increased base passage to 400px.
        // This ensures that even with high gravity, the "safe zone" is huge.
        double playerPassage = 400 + (currentSpeed * 0.05);

        double screenHeight = getAppHeight();
        double wallWidth = 60; // Thinner walls are easier to pass
        double playableHeight = screenHeight - (FLOOR_THICKNESS * 2);

        for (int i = 1; i <= 5; i++) {
            double spawnX = lastWall + i * gapBetweenObstacles;
            double chance = Math.random();

            if (chance < 0.30) {
                // DOUBLE SPIKES
                double totalWallSpace = playableHeight - playerPassage;

                // CENTERED HOLE: We limit the randomness so the hole
                // isn't tucked too far into a corner.
                double topHeight = random(totalWallSpace * 0.2, totalWallSpace * 0.8);
                double bottomHeight = totalWallSpace - topHeight;

                spawnSpike(spawnX, FLOOR_THICKNESS, wallWidth, topHeight, true);
                spawnSpike(spawnX, screenHeight - FLOOR_THICKNESS - bottomHeight, wallWidth, bottomHeight, false);

            } else if (chance < 0.65) {
                // FLOOR ONLY - Lowered height
                double h = random(80, 250);
                spawnSpike(spawnX, screenHeight - FLOOR_THICKNESS - h, wallWidth, h, false);

            } else if (chance < 0.95) {
                // CEILING ONLY - Lowered height
                double h = random(80, 250);
                spawnSpike(spawnX, FLOOR_THICKNESS, wallWidth, h, true);
            }
        }
        lastWall += 5 * gapBetweenObstacles;
    }

    private void spawnSpike(double x, double y, double w, double h, boolean pointingDown) {
        Point2D p1, p2, p3;

        if (pointingDown) {
            p1 = new Point2D(0, 0);
            p2 = new Point2D(w, 0);
            p3 = new Point2D(w / 2.0, h);
        } else {
            p1 = new Point2D(0, h);
            p2 = new Point2D(w / 2.0, 0);
            p3 = new Point2D(w, h);
        }

        entityBuilder()
                .at(x, y)
                .type(EntityType.WALL)
                .view(pointingDown ? spikeViewDown(w, h) : wallView(w, h))
                // Triangle Hitbox
                .bbox(new HitBox(BoundingShape.polygon(p1, p2, p3)))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }
}