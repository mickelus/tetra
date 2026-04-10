package se.mickelus.tetra.client.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

@MethodsReturnNonnullByDefault
public record PlainParticleOption(Vector3f color, float gravity, float friction) implements ParticleOptions {
    public static final MapCodec<PlainParticleOption> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(option -> option.color),
            com.mojang.serialization.Codec.FLOAT.fieldOf("gravity").forGetter(PlainParticleOption::gravity),
            com.mojang.serialization.Codec.FLOAT.fieldOf("friction").forGetter(PlainParticleOption::friction)
    ).apply(builder, PlainParticleOption::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlainParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F,
            PlainParticleOption::color,
            ByteBufCodecs.FLOAT,
            PlainParticleOption::gravity,
            ByteBufCodecs.FLOAT,
            PlainParticleOption::friction,
            PlainParticleOption::new
    );

    @Override
    public ParticleType<PlainParticleOption> getType() {
        return PlainParticleType.instance;
    }
}
