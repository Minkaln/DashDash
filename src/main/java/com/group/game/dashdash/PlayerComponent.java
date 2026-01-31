package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.*;

public class PlayerComponent extends Component {

    private final Vec2 velocity = new Vec2(200, 0);
    private double gravityDirection = 1.0;

    // --- NEW TUNED RATIO ---
    // Lower gravity means you stay in the air much longer while moving forward.
    private final double GRAVITY_FORCE = 1200;

    // Snappy flip speed to get you moving, but the gravity above will keep you floating.
    private final float JUMP_FORCE = 900;

    private boolean onSurface = false;

    @Override
    public void onAdded() {
        // Read mode and level from GameVars
        GameMode mode = geto("mode");
        int level = geti("level");

        // Boosted base speed to 550. Faster forward movement = easier to clear gaps.
        float speed = (mode == GameMode.Classic) ? (500f + (level * 50f)) : 550f;
        velocity.x = speed;
    }

    @Override
    public void onUpdate(double tpf) {
        // 1. If Endless, slowly increase speed over time
        if (geto("mode") == GameMode.Endless) {
            velocity.x += (float) (8 * tpf); // Speed up slightly faster
        }

        velocity.y += (GRAVITY_FORCE * gravityDirection * tpf);

        if (Math.abs(velocity.y) > JUMP_FORCE) {
            velocity.y = (float) (JUMP_FORCE * gravityDirection);
        }

        entity.translate(velocity.x * tpf, velocity.y * tpf);
        onSurface = false;
    }

    public void flipGravity() {
        if (onSurface) {
            gravityDirection *= -1;
            onSurface = false;

            // Apply the gentle jump force
            velocity.y = (float) (JUMP_FORCE * gravityDirection);

            entity.setScaleY(gravityDirection);
        }
    }

    public void setOnSurface(boolean onSurface) {
        this.onSurface = onSurface;
        if (onSurface) {
            velocity.y = 0;
        }
    }

    public double getVelocityX() {
        return velocity.x;
    }
}