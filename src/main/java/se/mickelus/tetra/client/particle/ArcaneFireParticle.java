package se.mickelus.tetra.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

@OnlyIn(Dist.CLIENT)
public record ArcaneFireParticle(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
    public static final String identifier = "arcane_fire";
    public static RegistryObject<SimpleParticleType> instance;

    @Override
    public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double tarX, double tarY,
            double tarZ) {
        TargetPointParticle particle = new TargetPointParticle(level, x, y, z, tarX, tarY, tarZ, 0.999f, -0.02f, 40, spriteSet)
                .withYD(0)
                .withColor(0xffbd94, 0xffbd94, 0xee5599);
        particle.setLifetime(80);
        return particle;
    }
}
