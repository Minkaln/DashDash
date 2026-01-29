package com.group.game.dashdash;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class WallBuildingComponent extends Component {

    private double lastWallX = 1000;
    private final double FLOOR_THICKNESS = 50; // same as floor

    @Override
    public void onUpdate(double tpf) {
        // Spawn walls ahead of the player
        if (lastWallX - entity.getX() < FXGL.getAppWidth()) {
            buildWalls();
        }
    }

    private Rectangle wallView(double width, double height) {
        Rectangle wall = new Rectangle(width, height);
        wall.setArcWidth(10);
        wall.setArcHeight(10);
        wall.fillProperty().bind(FXGL.getWorldProperties().objectProperty("stageColor"));
        return wall;
    }

    private void buildWalls() {
        double screenHeight = FXGL.getAppHeight();
        double wallHeight = 120;
        double wallWidth = 50;

        for (int i = 1; i <= 5; i++) {
            double spawnX = lastWallX + i * 600;

            double chance = Math.random();

            if (chance < 0.4) {
                // Floor wall
                entityBuilder()
                        .at(spawnX, screenHeight - FLOOR_THICKNESS - wallHeight)
                        .type(EntityType.WALL)
                        .viewWithBBox(wallView(wallWidth, wallHeight))
                        .with(new CollidableComponent(true))
                        .with(new WallComponent()) // ← moving
                        .buildAndAttach();

            } else if (chance < 0.8) {
                // Ceiling wall
                entityBuilder()
                        .at(spawnX, FLOOR_THICKNESS)
                        .type(EntityType.WALL)
                        .viewWithBBox(wallView(wallWidth, wallHeight))
                        .with(new CollidableComponent(true))
                        .with(new WallComponent()) // ← moving
                        .buildAndAttach();

            } // else 20% chance: gap (do nothing)
        }

        lastWallX += 5 * 600;
    }
}
