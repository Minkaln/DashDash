package com.group.game.dashdash;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.random;

public class WallBuildingComponent extends Component {

    private double lastWall = 1000;
    private final double FLOOR_THICKNESS = 50; // Matches your Floor component

    @Override
    public void onUpdate(double tpf) {
        if (lastWall - entity.getX() < FXGL.getAppWidth()) {
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
        double wallHeight = random(120, 550);
        double wallWidth = 50;

        for (int i = 1; i <= 5; i++) {
            double spawnX = lastWall + i * 600;

            // Generate a random number between 0.0 and 1.0
            double chance = Math.random();

            if (chance < 0.4) {
                // 40% chance: SPAWN ON FLOOR ONLY
                entityBuilder()
                        .at(spawnX, screenHeight - FLOOR_THICKNESS - wallHeight)
                        .type(EntityType.WALL)
                        .viewWithBBox(wallView(wallWidth, wallHeight))
                        .with(new CollidableComponent(true))
                        .buildAndAttach();

            } else if (chance < 0.8) {
                // 40% chance: SPAWN ON CEILING ONLY
                entityBuilder()
                        .at(spawnX, FLOOR_THICKNESS)
                        .type(EntityType.WALL)
                        .viewWithBBox(wallView(wallWidth, wallHeight))
                        .with(new CollidableComponent(true))
                        .buildAndAttach();

            } else  if (chance < 0.2) {
                entityBuilder()
                        .at(spawnX, screenHeight - FLOOR_THICKNESS - wallHeight)
                        .type(EntityType.WALL)
                        .viewWithBBox(wallView(wallWidth, random(wallHeight, 120)))
                        .with(new CollidableComponent(true))
                        .buildAndAttach();
                entityBuilder()
                        .at(spawnX, FLOOR_THICKNESS)
                        .type(EntityType.WALL)
                        .viewWithBBox(wallView(wallWidth, random(wallHeight, 120)))
                        .with(new CollidableComponent(true))
                        .buildAndAttach();
            }
            else {
                // 20% chance: SPAWN NOTHING (Empty space for the player to breathe)
                // We do nothing here, leaving a gap in the obstacles
            }
        }

        lastWall += 5 * 600;
    }
}