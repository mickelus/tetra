package se.mickelus.tetra.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public record SputteringPowerParticleProvider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {

    @Override
    public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double entityId, double unused1,
            double unused2) {
        Entity entity = level.getEntity((int) entityId);

        Supplier<Vec3> supplier = () -> null;
        if (entity != null) {
            float yOffset = entity.getBbHeight() * (0.1f + level.getRandom().nextFloat() * 0.8f);
            supplier = () -> entity.position().add(0, yOffset, 0);
        }

        Particle particle = new MovingTargetParticle(level, x, y, z, 0.9999f, -0.02f, 20, spriteSet, supplier)
                .withInitialSpeed(level.getRandom().nextGaussian() * 0.1, level.getRandom().nextGaussian() * 0.05, level.getRandom().nextGaussian() * 0.1)
                .withColor(0xffbd94, 0xffbd94, 0xee5599);
        particle.setLifetime(80);
        return particle;
    }

}
