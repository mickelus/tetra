package se.mickelus.tetra.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class MovingTargetParticle extends TargetPointParticle {

    private final Supplier<Vec3> target;

    MovingTargetParticle(ClientLevel level, double x, double y, double z, float friction, float gravity, int delay, SpriteSet spriteSet,
            Supplier<Vec3> target) {
        super(level, x, y, z, x, y, z, friction, gravity, delay, spriteSet);
        this.target = target;
        updateTarget();
    }

    @Override
    public void tick() {
        updateTarget();
        super.tick();
    }

    private void updateTarget() {
        Vec3 targetVector = target.get();
        if (targetVector != null) {
            tarX = targetVector.x;
            tarY = targetVector.y;
            tarZ = targetVector.z;
        }
    }
}
