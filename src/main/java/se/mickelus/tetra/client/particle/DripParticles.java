package se.mickelus.tetra.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

public class DripParticles {
    public static RegistryObject<SimpleParticleType> fallingBlood;
    public static RegistryObject<SimpleParticleType> landingBlood;
    public static RegistryObject<SimpleParticleType> fallingSlime;
    public static RegistryObject<SimpleParticleType> landingSlime;

    @OnlyIn(Dist.CLIENT)
    public static class FallingBloodProvider implements ParticleProvider<SimpleParticleType> {
        SpriteSet sprites;

        public FallingBloodProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType option, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz) {
            FallAndLandParticle particle = new FallAndLandParticle(level, x, y, z, Fluids.EMPTY, landingBlood.get(), this.sprites);
            particle.setParticleSpeed(dx, dy, dz);
            particle.setColor(0.72f, 0.14f, 0.14f);
            return particle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LandingBloodProvider implements ParticleProvider<SimpleParticleType> {
        SpriteSet sprites;

        public LandingBloodProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType option, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz) {
            DripLandParticle particle = new DripLandParticle(level, x, y, z, Fluids.EMPTY, this.sprites);
            particle.setColor(0.72f, 0.14f, 0.14f);
            return particle;
        }
    }

    static class FallAndLandParticle extends DripParticle.FallAndLandParticle {
        public FallAndLandParticle(ClientLevel pLevel, double pX, double pY, double pZ, Fluid pType, ParticleOptions pLandParticle,
                SpriteSet sprites) {
            super(pLevel, pX, pY, pZ, pType, pLandParticle);
            setSpriteFromAge(sprites);
        }
    }

    static class DripLandParticle extends DripParticle.DripLandParticle {
        public DripLandParticle(ClientLevel pLevel, double pX, double pY, double pZ, Fluid pType, SpriteSet sprites) {
            super(pLevel, pX, pY, pZ, pType);
            setSpriteFromAge(sprites);
        }
    }
}
