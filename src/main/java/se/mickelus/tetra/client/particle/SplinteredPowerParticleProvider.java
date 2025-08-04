package se.mickelus.tetra.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public record SplinteredPowerParticleProvider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
    @Override
    public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double tarX, double tarY,
            double tarZ) {
        TargetPointParticle particle = new TargetPointParticle(level, x, y, z, tarX, tarY + level.getRandom().nextFloat() * 0.8f, tarZ, 0.999f, -0.02f, 20, spriteSet)
                .withInitialSpeed(level.getRandom().nextGaussian() * 0.1, level.getRandom().nextGaussian() * 0.05, level.getRandom().nextGaussian() * 0.1)
                .withColor(0xffbd94, 0xffbd94, 0xee5599);
        particle.setLifetime(100);
        return particle;
    }
}
