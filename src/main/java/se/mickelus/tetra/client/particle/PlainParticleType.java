package se.mickelus.tetra.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class PlainParticleType extends ParticleType<PlainParticleOption> {
    public static final String identifier = "plain";

    @ObjectHolder(registryName = "particle_type", value = TetraMod.MOD_ID + ":" + identifier)
    public static ParticleType<PlainParticleOption> instance;

    public PlainParticleType() {
        super(true, PlainParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<PlainParticleOption> codec() {
        return PlainParticleOption.CODEC;
    }
}
