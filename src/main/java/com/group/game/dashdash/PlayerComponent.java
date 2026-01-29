package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import static com.almasb.fxgl.dsl.FXGL.*;

public class PlayerComponent extends Component {

    private Vec2 velocity = new Vec2(200, 0);
    private final double GRAVITY = 600;
    private boolean onGround = false; // NEW: Tracks if we are touching the floor

    @Override
    public void onUpdate(double tpf) {
        // 1. Only apply Gravity if we aren't on the ground
        if (!onGround) {
            velocity.y += GRAVITY * tpf;
        } else {
            velocity.y = 0; // Stop vertical movement when grounded
        }

        if (velocity.y > 500) velocity.y = 500;

        entity.translate(velocity.x * tpf, velocity.y * tpf);

        // 2. Check floor bounds (Emergency fall-back)
        if (entity.getBottomY() > getAppHeight()) {
            FXGL.<HelloApplication>getAppCast().requestNewGame();
        }

        if (entity.getY() < 0) {
            entity.setY(0);
            velocity.y = 0;
        }

        // 3. Reset onGround every frame.
        // If the collision handler doesn't set it to true next frame, we start falling.
        onGround = false;
    }

    public void jump()
    {
        if (onGround)
        {            // Only allow jump if touching the ground
            velocity.y = -1000;     // Jump velocity
            onGround = false;      // Leave the ground
            // Play sound
        }
    }



    // NEW: This will be called by your CollisionHandler in HelloApplication
    public void stopFalling() {
        onGround = true;
    }
}