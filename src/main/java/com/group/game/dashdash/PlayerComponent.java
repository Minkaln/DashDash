package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.*;

public class PlayerComponent extends Component {
    private Vec2 velocity = new Vec2(400, 0);
    private double gravityDirection = 1.0;
    private final double GRAVITY_FORCE = 4000;
    private boolean onSurface = false;

    @Override
    public void onAdded() {
        // Read mode and level from GameVars
        GameMode mode = geto("mode");
        int level = geti("level");

        // Set base speed: Level 1=450, Level 2=500, Level 3=550
        float speed = (mode == GameMode.Classic) ? (400f + (level * 50f)) : 400f;
        velocity.x = speed;
    }

    @Override
    public void onUpdate(double tpf) {
        // 1. If Endless, slowly increase speed over time
        if (geto("mode") == GameMode.Endless) {
            velocity.x += (float) (5 * tpf);
        }

        // 2. Apply gravity
        velocity.y += (float) (GRAVITY_FORCE * gravityDirection * tpf);

        // 3. Cap vertical speed (Increased to 700 to match higher horizontal speeds)
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

            // Launch the player to the other side
            // We scale the launch speed slightly with horizontal speed for better feel
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