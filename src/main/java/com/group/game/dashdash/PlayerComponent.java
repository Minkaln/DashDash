package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;

public class PlayerComponent extends Component {
    private Vec2 velocity = new Vec2(400, 0);
    private double gravityDirection = 1.0;
    private final double GRAVITY_FORCE = 4000;
    private boolean onSurface = false;

    @Override
    public void onUpdate(double tpf) {
        // Apply gravity
        velocity.y += (float) (GRAVITY_FORCE * gravityDirection * tpf);

        // Cap vertical speed
        if (Math.abs(velocity.y) > 700) {
            velocity.y = (float) (700 * gravityDirection);
        }

        entity.translate(velocity.x * tpf, velocity.y * tpf);
        onSurface = false;
    }

    public void flipGravity() {
        if (onSurface) {
            gravityDirection *= -1;
            onSurface = false;

            // --- THE FIX ---
            // Instead of waiting for gravity to pull us,
            // we immediately LAUNCH the player at high speed.
            velocity.y = (float) (1200 * gravityDirection);

            entity.setScaleY(gravityDirection);
        }
    }

    public void setOnSurface(boolean onSurface) {
        this.onSurface = onSurface;
        if (onSurface) {
            // This stops the velocity so we don't "vibrate" against the floor
            velocity.y = 0;
        }
    }
}