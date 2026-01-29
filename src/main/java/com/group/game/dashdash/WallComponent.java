package com.group.game.dashdash;

import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.*;

public class WallComponent extends Component {

    private double speed; // pixels per second

    @Override
    public void onAdded() {
        // Get wall speed from global world properties, default to 400
        if (getWorldProperties().exists("wallSpeed")) {
            speed = getWorldProperties().getDouble("wallSpeed");
        } else {
            speed = 400;
            getWorldProperties().setValue("wallSpeed", speed);
        }
    }

    @Override
    public void onUpdate(double tpf) {
        // Move wall left
        entity.translateX(-speed * tpf);

        // Optional: remove wall if off-screen to save memory
        if (entity.getX() + entity.getWidth() < 0) {
            entity.removeFromWorld();
        }
    }
}
