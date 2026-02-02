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
        if (lastWall - entity.getX() < getAppWidth()) {
            buildWalls();
        }
    }

    private Polygon wallView(double width, double height) {
        Polygon wall = new Polygon(
                0.0, height,
                width / 2.0, 0.0,
                width, height
        );
        wall.fillProperty().bind(getWorldProperties().objectProperty("stageColor"));
        return wall;
    }

    private Polygon spikeViewDown(double width, double height) {
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

        // 1. DYNAMIC GAP: The faster you go, the smaller the gap.
        // Starts at 450, but subtracts based on speed. Min-capped at 180 for fairness.
        double playerPassage = Math.max(180, 450 - (currentSpeed * 0.15));

        // 2. DYNAMIC SPACING: Distance between obstacles
        double gapBetweenObstacles = currentSpeed * 1.3;

        double screenHeight = getAppHeight();
        double wallWidth = 60;
        double playableHeight = screenHeight - (FLOOR_THICKNESS * 2);

        for (int i = 1; i <= 5; i++) {
            // 3. HORIZONTAL OFFSET: Spikes spawn slightly forward or backward
            // to break the "grid" feel.
            double horizontalOffset = random(-150, 150);
            double spawnX = lastWall + (i * gapBetweenObstacles) + horizontalOffset;

            double chance = Math.random();

            if (chance < 0.35) {
                // DOUBLE SPIKES (The Tunnel)
                double totalWallSpace = playableHeight - playerPassage;
                double topHeight = random(totalWallSpace * 0.15, totalWallSpace * 0.85);
                double bottomHeight = totalWallSpace - topHeight;

                spawnSpike(spawnX, FLOOR_THICKNESS, wallWidth, topHeight, true);
                spawnSpike(spawnX, screenHeight - FLOOR_THICKNESS - bottomHeight, wallWidth, bottomHeight, false);

            } else if (chance < 0.65) {
                // FLOOR ONLY
                double h = random(100, 300);
                spawnSpike(spawnX, screenHeight - FLOOR_THICKNESS - h, wallWidth, h, false);

            } else if (chance < 0.95) {
                // CEILING ONLY
                double h = random(100, 300);
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
                .bbox(new HitBox(BoundingShape.polygon(p1, p2, p3)))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }
}