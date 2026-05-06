package se.mickelus.tetra.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@MethodsReturnNonnullByDefault
public record SweepingStrikeParticleOption(int duration, boolean reverse, float pitch, float yaw) implements ParticleOptions {
    public static final MapCodec<SweepingStrikeParticleOption> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("duration").forGetter(SweepingStrikeParticleOption::duration),
            Codec.BOOL.fieldOf("reverse").forGetter(SweepingStrikeParticleOption::reverse),
            Codec.FLOAT.fieldOf("pitch").forGetter(SweepingStrikeParticleOption::pitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(SweepingStrikeParticleOption::yaw)
    ).apply(builder, SweepingStrikeParticleOption::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SweepingStrikeParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SweepingStrikeParticleOption::duration,
            ByteBufCodecs.BOOL,
            SweepingStrikeParticleOption::reverse,
            ByteBufCodecs.FLOAT,
            SweepingStrikeParticleOption::pitch,
            ByteBufCodecs.FLOAT,
            SweepingStrikeParticleOption::yaw,
            SweepingStrikeParticleOption::new
    );

    @Override
    public ParticleType<SweepingStrikeParticleOption> getType() {
        return SweepingStrikeParticleType.instance;
    }
}
