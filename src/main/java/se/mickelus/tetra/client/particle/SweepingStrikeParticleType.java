package se.mickelus.tetra.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import se.mickelus.tetra.compat.forge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class SweepingStrikeParticleType extends ParticleType<SweepingStrikeParticleOption> {
    public static final String identifier = "sweeping_strike";

    @ObjectHolder(registryName = "particle_type", value = TetraMod.MOD_ID + ":" + identifier)
    public static ParticleType<SweepingStrikeParticleOption> instance;

    public SweepingStrikeParticleType() {
        super(true);
    }

    @Override
    public MapCodec<SweepingStrikeParticleOption> codec() {
        return SweepingStrikeParticleOption.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, SweepingStrikeParticleOption> streamCodec() {
        return SweepingStrikeParticleOption.STREAM_CODEC;
    }
}
