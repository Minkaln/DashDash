package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.*;

public class PlayerComponent extends Component {

    private final Vec2 velocity = new Vec2(200, 0);
    private double gravityDirection = 1.0;

    private final double GRAVITY_FORCE = 1200;
    private final float JUMP_FORCE = 900;

    private boolean onSurface = false;

    @Override
    public void onAdded() {
        velocity.x = 550f;
    }

    @Override
    public void onUpdate(double tpf) {
        // --- FIX STARTS HERE ---
        // Cap the tpf to roughly 60 FPS (0.017 seconds).
        // This prevents the "speed burst" after resuming from a dialog box.
        double fixedTpf = Math.min(tpf, 0.017);
        // --- FIX ENDS HERE ---

        // Use fixedTpf for speed increase
        velocity.x += (float) (8 * fixedTpf);

        // Use fixedTpf for gravity calculations
        velocity.y += (GRAVITY_FORCE * gravityDirection * fixedTpf);

        if (Math.abs(velocity.y) > JUMP_FORCE) {
            velocity.y = (float) (JUMP_FORCE * gravityDirection);
        }

        // Use fixedTpf for actual movement
        entity.translate(velocity.x * fixedTpf, velocity.y * fixedTpf);

        onSurface = false;
    }

    public void flipGravity() {
        if (onSurface) {
            gravityDirection *= -1;
            onSurface = false;
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