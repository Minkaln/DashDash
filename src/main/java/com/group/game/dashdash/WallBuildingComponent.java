package com.group.game.dashdash;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

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
        double height = FXGL.getAppHeight();
        double distance = height / 2;

        for (int i = 1; i <= 10; i++) {
            double topHeight = Math.random() * (height - distance);

            entityBuilder()
                    .at(lastWall + i * 500, 0 - 25)
                    .type(EntityType.WALL)
                    .viewWithBBox(wallView(50, topHeight))
                    .with(new CollidableComponent(true))
                    .buildAndAttach();

            entityBuilder()
                    .at(lastWall + i * 500, 0 + topHeight + distance + 25)
                    .type(EntityType.WALL)
                    .viewWithBBox(wallView(50, height - distance - topHeight))
                    .with(new CollidableComponent(true))
                    .buildAndAttach();
        }

        lastWall += 10 * 500;
    }
}
