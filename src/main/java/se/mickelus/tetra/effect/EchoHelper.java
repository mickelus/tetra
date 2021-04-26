package se.mickelus.tetra.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.ServerScheduler;

import java.util.Random;

public class EchoHelper {
    public static void echo(PlayerEntity attacker, int delay, Runnable callback) {
        Vector3d origin = attacker.getPositionVec();
        Random rand = attacker.getRNG();
        for (int i = 0; i < delay / 10; i++) {
            ServerScheduler.schedule(i * 10, () -> {
                ((ServerWorld) attacker.world).spawnParticle(ParticleTypes.WITCH,
                        origin.x + attacker.getWidth() * (rand.nextGaussian() - 0.5) * 0.5,
                        origin.y + attacker.getHeight() * rand.nextFloat(),
                        origin.z + attacker.getWidth() * (rand.nextGaussian() - 0.5) * 0.5,
                        3, rand.nextFloat() * 0.2, 0.2 + rand.nextFloat() * 0.6, rand.nextFloat() * 0.2, 0);
            });
        }

        ServerScheduler.schedule(delay, () -> {
                Vector3d currentPos = attacker.getPositionVec();
                attacker.setPosition(origin.x, origin.y, origin.z);
                callback.run();
                attacker.setPosition(currentPos.x, currentPos.y, currentPos.z);
        });
    }
}
