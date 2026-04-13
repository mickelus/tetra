package se.mickelus.tetra.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import se.mickelus.tetra.TetraMod;

public class PlainParticleType extends ParticleType<PlainParticleOption> {
    public static final String identifier = "plain";

    public static ParticleType<PlainParticleOption> instance;

    public PlainParticleType() {
        super(true);
    }

    @Override
    public MapCodec<PlainParticleOption> codec() {
        return PlainParticleOption.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, PlainParticleOption> streamCodec() {
        return PlainParticleOption.STREAM_CODEC;
    }
}
