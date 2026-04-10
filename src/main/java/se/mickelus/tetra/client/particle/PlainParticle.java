package se.mickelus.tetra.client.particle;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@MethodsReturnNonnullByDefault
public class PlainParticle extends SimpleAnimatedParticle {
    protected PlainParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, PlainParticleOption options,
            SpriteSet sprites) {
        super(level, x, y, z, sprites, 0);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.rCol = options.color().x();
        this.gCol = options.color().y();
        this.bCol = options.color().z();
        this.quadSize = 1 / 8f;
        this.lifetime = 100;
        this.setSpriteFromAge(sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<PlainParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(PlainParticleOption option, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz) {
            return new PlainParticle(level, x, y, z, dx, dy, dz, option, this.sprites);
        }
    }
}
