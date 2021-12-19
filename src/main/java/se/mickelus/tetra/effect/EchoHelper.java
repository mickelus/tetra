package se.mickelus.tetra.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.ServerScheduler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class EchoHelper {
    public static void echo(Player attacker, int delay, Runnable callback) {
        Vec3 origin = attacker.position();
        Random rand = attacker.getRandom();
        for (int i = 0; i < delay / 10; i++) {
            ServerScheduler.schedule(i * 10, () -> {
                ((ServerLevel) attacker.level).sendParticles(ParticleTypes.WITCH,
                        origin.x + attacker.getBbWidth() * (rand.nextGaussian() - 0.5) * 0.5,
                        origin.y + attacker.getBbHeight() * rand.nextFloat(),
                        origin.z + attacker.getBbWidth() * (rand.nextGaussian() - 0.5) * 0.5,
                        3, rand.nextFloat() * 0.2, 0.2 + rand.nextFloat() * 0.6, rand.nextFloat() * 0.2, 0);
            });
        }

        ServerScheduler.schedule(delay, () -> {
            Vec3 currentPos = attacker.position();
            attacker.setPos(origin.x, origin.y, origin.z);
            callback.run();
            attacker.setPos(currentPos.x, currentPos.y, currentPos.z);
        });
    }
}
