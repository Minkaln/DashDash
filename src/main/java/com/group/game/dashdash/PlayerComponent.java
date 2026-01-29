package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;

public class PlayerComponent extends Component {

    private final Vec2 velocity = new Vec2(200, 0); // start slower
    private double gravityDirection = 1.0;
    private final double GRAVITY_FORCE = 4000;
    private boolean onSurface = false;

    private double acceleration = 50; // how fast horizontal speed increases (pixels/sec²)
    private double maxSpeed = 1200;   // maximum horizontal speed

    @Override
    public void onUpdate(double tpf) {

        // --- Increase horizontal speed gradually ---
        if (velocity.x < maxSpeed) {
            velocity.x += acceleration * tpf;
            if (velocity.x > maxSpeed) {
                velocity.x = (float) maxSpeed;
            }
        }

        // Apply gravity
        velocity.y += GRAVITY_FORCE * gravityDirection * tpf;

        // Cap vertical speed
        if (Math.abs(velocity.y) > 500) {
            velocity.y = (float) (2000 * gravityDirection);
        }

        // Move player
        entity.translate(velocity.x * tpf, velocity.y * tpf);

        // Reset onSurface for next frame
        onSurface = false;
    }

    public void flipGravity() {
        if (onSurface) {
            gravityDirection *= -1;
            onSurface = false;

            // Launch player instantly
            velocity.y = (float) (1200 * gravityDirection);
            entity.setScaleY(gravityDirection);
        }
    }

    public void setOnSurface(boolean onSurface) {
        this.onSurface = onSurface;
        if (onSurface) {
            velocity.y = 0;
        }
    }
}
