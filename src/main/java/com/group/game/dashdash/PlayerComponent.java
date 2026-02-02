package com.group.game.dashdash;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;

public class PlayerComponent extends Component {

    private final Vec2 velocity = new Vec2(200, 0);
    private double gravityDirection = 1.0;

    private final float JUMP_FORCE = 900;

    private boolean onSurface = false;

    @Override
    public void onAdded() {
        velocity.x = 550f;
    }

    @Override
    public void onUpdate(double tpf) {
        double fixedTpf = Math.min(tpf, 0.017);
        velocity.x += (float) (8 * fixedTpf);
        double GRAVITY_FORCE = 1200;
        velocity.y += (float) (GRAVITY_FORCE * gravityDirection * fixedTpf);

        if (Math.abs(velocity.y) > JUMP_FORCE) {
            velocity.y = (float) (JUMP_FORCE * gravityDirection);
        }

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

    // RENAMED to avoid "Ambiguous" error
    public void setTouchingSurface(boolean touching) {
        this.onSurface = touching;
        if (touching) {
            velocity.y = 0;
        }
    }

    public boolean isOnSurface() {
        return onSurface;
    }

    public double getVelocityX() {
        return velocity.x;
    }
}