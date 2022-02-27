package se.mickelus.tetra.blocks.geode.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SparkleParticle extends SimpleAnimatedParticle {
    SparkleParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet, 0);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.quadSize = 1 / 8f;
        this.lifetime = 5 + this.random.nextInt(12);
        this.setFadeColor(0xbfbb8e);
        this.setSpriteFromAge(spriteSet);
    }

    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new SparkleParticle(level, x, y, z, dx, dy, dz, this.sprites);
        }
    }
}
