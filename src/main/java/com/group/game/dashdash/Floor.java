package com.group.game.dashdash;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Floor extends Component {

    private double lastFloorX = 0;
    private final double FLOOR_HEIGHT = 50;

    @Override
    public void onUpdate(double tpf) {
        // If the end of our last floor piece is getting close to the screen edge
        // We use entity.getX() which refers to the Player since this is attached to them
        if (lastFloorX - entity.getX() < FXGL.getAppWidth()) {
            buildFloor();
        }
    }

    private Rectangle floorView(double width, double height) {
        Rectangle rect = new Rectangle(width, height);
        rect.setArcWidth(1);
        rect.setArcHeight(1);
        rect.fillProperty().bind(FXGL.getWorldProperties().objectProperty("stageColor"));
        return rect;
    }

    private void buildFloor() {
        double screenWidth = FXGL.getAppWidth();
        double screenHeight = FXGL.getAppHeight();

        // Spawn 5 segments at a time
        for (int i = 0; i < 5; i++) {

            // 1. THE BOTTOM FLOOR
            entityBuilder()
                    .at(lastFloorX, screenHeight - FLOOR_HEIGHT)
                    .type(EntityType.FLOOR)
                    .viewWithBBox(floorView(screenWidth, FLOOR_HEIGHT))
                    .collidable()
                    .buildAndAttach();

            // 2. THE TOP CEILING
            entityBuilder()
                    .at(lastFloorX, 0) // Positioned at y = 0
                    .type(EntityType.FLOOR)
                    .viewWithBBox(floorView(screenWidth, FLOOR_HEIGHT))
                    .collidable()
                    .buildAndAttach();

            lastFloorX += screenWidth;
        }
    }
}